package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.admin.StaffCreateRequest;
import com.example.bookingtour.dtos.request.admin.StaffUpdateRequest;
import com.example.bookingtour.dtos.request.auth.LoginRequest;
import com.example.bookingtour.dtos.request.auth.RegisterRequest;
import com.example.bookingtour.dtos.request.auth.IntrospectRequest;
import com.example.bookingtour.dtos.response.auth.AuthenticationResponse;
import com.example.bookingtour.dtos.response.auth.IntrospectResponse;
import com.example.bookingtour.dtos.response.auth.UserResponse;
import com.example.bookingtour.entities.User;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.entities.InvalidatedToken;
import com.example.bookingtour.enums.UserStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    DepartmentRepository departmentRepository;
    CustomerProfileRepository customerProfileRepository;
    StaffProfileRepository staffProfileRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return AuthenticationResponse.builder()
                .token(generateAccessToken(user))
                .refreshToken(generateRefreshToken(user))
                .authenticated(true)
                .build();
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        var role = roleRepository.findById("CUSTOMER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        var newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .userCode("CUS-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .build();

        newUser = userRepository.save(newUser);

        var customerProfile = CustomerProfile.builder()
                .user(newUser)
                .fullName(request.getFullName())
                .phone(request.getPhoneNumber())
                .build();

        customerProfileRepository.save(customerProfile);

        return AuthenticationResponse.builder().authenticated(true).build();
    }
    public String generateAccessToken(User user) {

        return generateToken(user, VALID_DURATION, "access");
    }

    public String generateRefreshToken(User user) {

        return generateToken(user, REFRESHABLE_DURATION, "refresh");
    }

    private String generateToken(User user, long duration, String type) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim("userId", user.getId())
                .claim("userCode", user.getUserCode())
                .subject(user.getEmail())
                .issuer("bookingtour.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("type", type)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        if (user.getRole() != null) {
            joiner.add("ROLE_" + user.getRole().getRoleName());
        }
        return joiner.toString();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            verifyToken(request.getToken(), false);
            return IntrospectResponse.builder().valid(true).build();
        } catch (AppException | JOSEException | ParseException e) {
            return IntrospectResponse.builder().valid(false).build();
        }
    }

    public void logout(String token) {
        try {
            var signedJWT = verifyToken(token, false);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiry = signedJWT.getJWTClaimsSet().getExpirationTime();

            invalidatedTokenRepository.save(InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiry)
                    .build());
        } catch (AppException | ParseException | JOSEException e) {
            log.info("Token invalid: {}", e.getMessage());
        }
    }

    public AuthenticationResponse refreshToken(String refreshToken) throws ParseException, JOSEException {
        var signedJWT = verifyToken(refreshToken, true);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiry = signedJWT.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder().id(jti).expiryTime(expiry).build());

        var user = userRepository.findByEmail(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        return AuthenticationResponse.builder()
                .token(generateAccessToken(user))
                .refreshToken(generateRefreshToken(user))
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean allowExpiredForRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = allowExpiredForRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);
        if (!verified || !expiryTime.after(new Date()) || invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}