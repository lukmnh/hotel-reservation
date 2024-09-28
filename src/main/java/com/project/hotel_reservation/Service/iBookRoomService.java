package com.project.hotel_reservation.Service;

import java.util.List;

import com.project.hotel_reservation.Model.BookedRoom;

public interface iBookRoomService {
    public List<BookedRoom> getAllBookingsByRoomId(Long id);
}
