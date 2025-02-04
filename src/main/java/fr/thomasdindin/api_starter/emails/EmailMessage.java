package fr.thomasdindin.api_starter.emails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class EmailMessage implements Serializable {
    private String recipient;
    private String code;
}
