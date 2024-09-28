package com.project.hotel_reservation.Service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.project.hotel_reservation.DTO.RoomResponse;
import com.project.hotel_reservation.Exception.FetchPhotoException;
import com.project.hotel_reservation.Exception.InternalServerException;
import com.project.hotel_reservation.Exception.ResourceNotFoundException;
import com.project.hotel_reservation.Model.BookedRoom;
import com.project.hotel_reservation.Model.Room;
import com.project.hotel_reservation.Repository.RoomRepository;
import com.project.hotel_reservation.Service.iBookRoomService;
import com.project.hotel_reservation.Service.iRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class RoomServiceImpl implements iRoomService {

    private final RoomRepository roomRepository;

    private final iBookRoomService bookRoomService;

    @Autowired
    TransactionTemplate template;

    @Autowired
    private DataSource dataSource;

    // transactional to add new room like suite, single, double, or vip
    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        log.info("room type is {}", roomType);
        room.setRoomPrice(roomPrice);
        log.info("room price is {}", roomPrice);
        if (!file.isEmpty()) {
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }
        return roomRepository.save(room);
    }

    // fetch all room types
    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypeS();
    }

    // fetch all room from repo
    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long id) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(id);
        if (theRoom.isEmpty()) {
            throw new ResourceNotFoundException("Sorry, Room not found");
        }

        Blob photoBlob = theRoom.get().getPhoto();
        if (photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }
        return null;
    }

    @Override
    @Transactional
    public List<RoomResponse> getAllRoomResponse() {
        // call method get all room to call all room from db
        List<Room> listOfRoom = getAllRooms();
        List<RoomResponse> respOfRoom = new ArrayList<>();
        try {
            // fetching the list of room and then convert into roomresponse
            for (Room room : listOfRoom) {
                RoomResponse roomResp = getRoomResponse(room);
                respOfRoom.add(roomResp);
            }
            log.info("parameter {}", respOfRoom);
            return respOfRoom;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("cannot get a room", e);
            throw new ResourceNotFoundException("Failed to get room");
        }
    }

    // convert entity room into dto resp
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        // List<BookedRoomResponse> bookedInfo = bookings
        // .stream()
        // .map(book -> new BookedRoomResponse(book.getBookingId(),
        // book.getCheckInDate(), book.getCheckOutDate(),
        // book.getBookingConfirmationCode()))
        // .toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        try {
            if (photoBlob != null) {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("failed to fetch photo for room id", room.getId(), e);
            throw new FetchPhotoException("Failed to fetch photo");
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes);
    }

    // fetch all bookings for given room id
    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        List<BookedRoom> result = new ArrayList<>();
        try {
            result = bookRoomService.getAllBookingsByRoomId(roomId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void deleteRoom(Long roomId) {
        try {
            Room getRoomId = roomRepository.findById(roomId)
                    .orElseThrow(() -> new NoSuchElementException("Room not found: ID " + roomId));
            roomRepository.delete(getRoomId);
            log.info("success deleted room id of {}", roomId);
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }

    }

    @SuppressWarnings("deprication")
    @Transactional
    @Override
    public RoomResponse getRoomById(Long roomId) {
        try {
            // Fetch the room by ID
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room ID " + roomId + " not found"));

            // Create the RoomResponse from the Room object
            RoomResponse response = getRoomResponse(room);

            // Get the photo data
            byte[] photoBytes = getRoomPhotoByRoomId(roomId);

            // If the photoBytes are not null, convert to Base64 and set in response
            if (photoBytes != null) {
                String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
                response.setPhoto(base64Photo); // Assuming setPhoto accepts Base64 string
            } else {
                response.setPhoto(null); // Handle case where photo is null
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new ResourceNotFoundException("failed to get room");
        }
    }

    @Transactional
    @Override
    public RoomResponse updateRoom(Long roomId, String roomType, BigDecimal roomPrice, MultipartFile photo) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorry, Room not found!"));

        try {
            if (roomType != null) {
                room.setRoomType(roomType);
            } else {
                log.warn("Room type for room ID {} is null; no update will be made.", roomId);
            }

            if (roomPrice != null) {
                room.setRoomPrice(roomPrice);
            } else {
                log.warn("Room price for room ID {} is null; no update will be made.", roomId);
            }

            if (photo != null && !photo.isEmpty()) {
                try {
                    byte[] photoBytes = photo.getBytes();
                    // Convert byte[] to Blob
                    room.setPhoto(new javax.sql.rowset.serial.SerialBlob(photoBytes));
                } catch (IOException | SQLException ex) {
                    throw new InternalServerException("Failed to update room photo");
                }
            }
            // Save the updated room
            room = roomRepository.save(room);
            // convert room to RoomResponse
            return getRoomResponse(room);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to updated room", e.getMessage());
            throw new InternalServerException("Failed to updated room");
        }
    }

}