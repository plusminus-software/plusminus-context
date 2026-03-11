package software.plusminus.transaction.context.fixtures;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionalService {

    @Transactional
    public void inTransaction(Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inNewTransaction(Runnable runnable) {
        runnable.run();
    }
}
