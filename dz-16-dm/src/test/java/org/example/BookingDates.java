package org.example;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class BookingDates {
    private Date checkin;
    private Date checkout;
}
