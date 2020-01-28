package edu.vt.datasheet_text_processor.tokens.TokenInstance;

public class BitIndex {
    private Integer bitx;
    private Integer bity;

    public BitIndex(){}

    public BitIndex(Integer bitx) {
        this.bitx = bitx;
    }

    public BitIndex(Integer bitx, Integer bity) {
        this.bitx = bitx;
        this.bity = bity;
    }

    private boolean checkBitx(BitIndex other) {
        if (bitx == null) {
            return other.getBitx() == null;
        } else {
            return bitx.equals(other.getBitx());
        }
    }

    private boolean checkBity(BitIndex other) {
        if (bity == null) {
            return other.getBity() == null;
        } else {
            return bity.equals(other.getBity());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitIndex) {
            var newO = (BitIndex) obj;
            return checkBitx(newO) && checkBity(newO);
        } else {
            return false;
        }
    }

    public Integer getBitx() {
        return bitx;
    }

    public void setBitx(Integer bitx) {
        this.bitx = bitx;
    }

    public Integer getBity() {
        return bity;
    }

    public void setBity(Integer bity) {
        this.bity = bity;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(bitx);
        if (bity != null) {
            sb.append(":");
            sb.append(bity);
        }
        return sb.toString();
    }
}
