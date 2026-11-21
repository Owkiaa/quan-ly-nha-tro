package com.nhatro.exception;

/**
 * Ngoại lệ ném ra khi phòng đã có người đặt trước hoặc đang thuê (phòng không còn trống).
 */
public class RoomAlreadyBookedException extends BusinessException {

    public RoomAlreadyBookedException(String message) {
        super(message);
    }
}
