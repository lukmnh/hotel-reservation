package com.project.hotel_reservation.Service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.hotel_reservation.Model.BookedRoom;
import com.project.hotel_reservation.Service.iBookRoomService;

@Service
public class BookedRoomServiceImpl implements iBookRoomService {

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long id) {
        return null;
    }

}
