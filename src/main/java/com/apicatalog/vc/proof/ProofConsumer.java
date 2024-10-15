package com.apicatalog.vc.proof;

import java.util.function.Consumer;

@FunctionalInterface
public interface ProofConsumer  extends Consumer<Proof> {

    @Override
    void accept(Proof proof);

}
