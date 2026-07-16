package com.apicatalog.di.barcodes;

import java.util.Collection;

import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.DigestiblePayload;

public class OpticalBarcode implements DigestiblePayload {

    byte[] canonicalPayload;
    byte[] opticalData;

    public OpticalBarcode(byte[] canonicalPayload) {
        this.canonicalPayload = canonicalPayload;
    }

    @Override
    public byte[] canonicalPayload() {
        return canonicalPayload;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

    @Override
    public void digest(String algorithm, byte[] value) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] digest(String algorithm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> digestAlgorithms() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] opticalData() {
        return opticalData;
    }

    public void opticalData(byte[] opticalData) {
        this.opticalData = opticalData;
    }

}
