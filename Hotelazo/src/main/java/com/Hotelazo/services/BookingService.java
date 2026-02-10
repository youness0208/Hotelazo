package com.Hotelazo.services;

import com.Hotelazo.dtos.BookingDTO;
import com.Hotelazo.dtos.Response;

public interface BookingService {

    Response getAllBookings();
    Response createBooking(BookingDTO bookingDTO);
    Response findBookingByReferenceNo(String  bookingReference);
    Response updateBooking(BookingDTO bookingDTO);
}
