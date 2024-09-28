package com.project.hotel_reservation.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.hotel_reservation.DTO.RoomResponse;
import com.project.hotel_reservation.Exception.ResourceNotFoundException;
import com.project.hotel_reservation.Model.Room;
import com.project.hotel_reservation.Service.iRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("deprecation")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private final iRoomService roomService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(@RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(),
                savedRoom.getRoomType(),
                savedRoom.getRoomPrice());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRoom() {
        try {
            List<RoomResponse> respOfRoom = roomService.getAllRoomResponse();
            return ResponseEntity.ok(respOfRoom);
        } catch (Exception e) {
            log.info("error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (NoSuchElementException e) {
            log.error("Room not found: ID {}", roomId, e);
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception e) {
            log.error("Error deleting room: ID {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    @GetMapping("/getRoomById/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        try {
            RoomResponse getId = roomService.getRoomById(id);
            return ResponseEntity.ok(getId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id,
            @RequestParam(required = false) String roomType, @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException {

        try {
            RoomResponse roomResponse = roomService.updateRoom(id, roomType, roomPrice, photo);
            log.info("data room updated successfully id : {}, roomType : {}, roomPrice : {}", id, roomType, roomPrice);
            return ResponseEntity.ok(roomResponse);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("failed to update the room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // @GetMapping("/images/{roomId}")
    // public ResponseEntity<byte[]> getRoomImage(@PathVariable Long roomId) {
    // try {
    // byte[] imageBytes = roomService.getRoomPhotoByRoomId(roomId);
    // if (imageBytes != null) {
    // return ResponseEntity.ok(imageBytes);
    // } else {
    // return ResponseEntity.notFound().build();
    // }
    // } catch (SQLException e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    // }
    // }

}
