package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAccumulator;

public class PriceAggregator {
    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            final var min = new DoubleAccumulator(Double::min, Double.MAX_VALUE);

            shopIds.stream()
                    .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId))
                            .exceptionally(throwable -> Double.MAX_VALUE))
                    .forEach(priceFuture -> min.accumulate(priceFuture.join()));

            return min.get();
        }).completeOnTimeout(Double.NaN, 2900, TimeUnit.MILLISECONDS).join();
    }
}
