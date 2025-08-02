package com.notesSharingApp.notesSharingApp.DTO;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RemarkRequest {
    private String id;
    private String message;
}
