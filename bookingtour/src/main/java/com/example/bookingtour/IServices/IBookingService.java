package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.booking.BookingCancelRequest;
import com.example.bookingtour.dtos.request.booking.BookingCreateRequest;
import com.example.bookingtour.dtos.request.payment.ManualPaymentRequest;
import com.example.bookingtour.dtos.response.booking.BookingResponse;
import com.example.bookingtour.dtos.response.payment.PaymentResponse;

import java.util.List;

public interface IBookingService {

    BookingResponse createBooking(BookingCreateRequest request, Integer userId);


    BookingResponse getBookingById(Integer bookingId);


    List<BookingResponse> getBookingsByUser(Integer userId);


    BookingResponse cancelBooking(BookingCancelRequest request);

}