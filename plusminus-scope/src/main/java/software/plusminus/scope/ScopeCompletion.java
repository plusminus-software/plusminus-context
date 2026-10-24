package software.plusminus.scope;

/**
 * Finishes a scope started with {@link ScopeRunner#runDeferred}. Only the first call has an effect.
 */
public interface ScopeCompletion {

    /**
     * Publishes the completed and finalized events.
     */
    void complete();

    /**
     * Publishes the failed and finalized events.
     */
    void fail(Throwable failure);
}
