package com.project.hotel_reservation.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.hotel_reservation.DTO.RoomResponse;
import com.project.hotel_reservation.Model.Room;

public interface iRoomService {

    Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException;

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    public List<RoomResponse> getAllRoomResponse();

    byte[] getRoomPhotoByRoomId(Long id) throws SQLException;

    void deleteRoom(Long roomId);

    public RoomResponse getRoomById(Long roomId);

    RoomResponse updateRoom(Long id, String roomType, BigDecimal roomPrice, MultipartFile photo)
            throws SQLException, IOException;
}
