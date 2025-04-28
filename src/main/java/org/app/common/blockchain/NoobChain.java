package org.app.common.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class NoobChain {

    public static final int DIFFICULTY = 5;

    public static Boolean isChainValid(ArrayList<Block> blockchain) {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');

        //loop through blockchain to check hashes:
        int size = blockchain.size();
        for (int i = 1; i < size; i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.hash())) {
                log.error("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                log.error("Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.hash.substring(0, DIFFICULTY).equals(hashTarget)) {
                log.error("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

}
