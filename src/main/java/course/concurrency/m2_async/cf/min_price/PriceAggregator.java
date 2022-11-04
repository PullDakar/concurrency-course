package course.concurrency.m2_async.cf.min_price;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PriceAggregator {
    private static final int SLA = 3000;
    private static final int EXTRA = 100;
    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        return Arrays.stream(shopIds.stream()
                        .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId)))
                        .map(priceFuture -> priceFuture
                                .orTimeout(SLA - EXTRA, TimeUnit.MILLISECONDS)
                                .exceptionally(e -> null))
                        .toArray(CompletableFuture[]::new))
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .mapToDouble(price -> (Double) price)
                .min()
                .orElse(Double.NaN);
    }
}
