package software.plusminus.scope.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InvocationFailedEvent {

    private Exception exception;

}
