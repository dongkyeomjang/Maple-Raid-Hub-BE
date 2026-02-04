package com.mapleraid.character.application.port.in.input.query;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadMyCharactersInput {

    private final UserId userId;

    public ReadMyCharactersInput(
            UserId userId
    ) {
        this.userId = userId;
    }

    public static ReadMyCharactersInput of(
            UserId userId
    ) {
        return new ReadMyCharactersInput(
                userId
        );
    }
}
