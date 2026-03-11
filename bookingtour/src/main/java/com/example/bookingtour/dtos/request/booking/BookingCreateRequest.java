package com.example.bookingtour.dtos.request.booking;

import com.example.bookingtour.dtos.request.profile.CustomerUpdateProfileRequest;
import com.example.bookingtour.entities.CustomerProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Vui lòng chọn lịch khởi hành")
    private Integer scheduleId;

    private String voucherCode;

    private String email;

    @Valid
    @NotNull(message = "Thông tin người liên hệ không được để trống")
    private CustomerUpdateProfileRequest customerProfile;

    @NotEmpty(message = "Danh sách hành khách không được để trống")
    private List<PassengerRequest> passengers;


}