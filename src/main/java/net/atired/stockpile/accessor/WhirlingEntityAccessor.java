package net.atired.stockpile.accessor;

import java.util.List;

public interface WhirlingEntityAccessor {
    void stockpile$addToEntityHit(int id);
    void stockpile$clearHitList();
    void stockpile$removeground();
    void stockpile$setUnchainedTicks(int chained);
    int stockpile$getUnchainedTicks();
    int stockpile$getSpinAge();
    void stockpile$setSpinAge(int ticks);
    boolean stockpile$inGround();
    List<Integer> stockpile$getEntityHitList();
}
