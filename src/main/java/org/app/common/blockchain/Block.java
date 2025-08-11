package org.app.common.blockchain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.PasswordCrypto;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Getter
@Setter
@Slf4j
public class Block {
    public String hash;
    public String previousHash;
    private String data; //our data will be a simple message.
    private long timeStamp; //as number of milliseconds since 1/1/1970.
    private int nonce;

    //Block Constructor.
    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = hash();
    }

    public @NotNull String hash() {
        return PasswordCrypto.sha256(previousHash + timeStamp + data);
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = hash();
        }
        log.info("Block Mined!!! : {}", hash);
    }
}
