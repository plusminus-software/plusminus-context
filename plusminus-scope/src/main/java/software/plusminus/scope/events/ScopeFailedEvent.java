package software.plusminus.scope.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScopeFailedEvent {

    private Exception e;

}
