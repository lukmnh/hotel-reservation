package com.project.hotel_reservation.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.hotel_reservation.Model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("select distinct r.roomType from Room r")
    List<String> findDistinctRoomTypeS();
}
