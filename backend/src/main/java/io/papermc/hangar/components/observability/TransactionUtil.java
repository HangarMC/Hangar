package io.papermc.hangar.components.observability;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import io.sentry.TransactionOptions;

public final class TransactionUtil {

    public static void withTransaction(String op, String name, Runnable runnable) {
        TransactionOptions transactionOptions = new TransactionOptions();
        transactionOptions.setBindToScope(true);
        ITransaction transaction = Sentry.startTransaction(name, op, transactionOptions);
        try {
            runnable.run();
        } catch (Exception e) {
            transaction.setThrowable(e);
            transaction.setStatus(SpanStatus.INTERNAL_ERROR);
            throw e;
        } finally {
            transaction.finish();
        }
    }
}
