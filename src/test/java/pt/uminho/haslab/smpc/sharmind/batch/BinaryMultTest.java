package pt.uminho.haslab.smpc.sharmind.batch;

import org.junit.runners.Parameterized;
import pt.uminho.haslab.smpc.interfaces.Player;
import pt.uminho.haslab.smpc.interfaces.SharedSecret;
import pt.uminho.haslab.smpc.sharemindImp.BigInteger.SharemindSecretFunctions;
import pt.uminho.haslab.smpc.sharemindImp.BigInteger.SharemindSharedSecret;
import pt.uminho.haslab.smpc.sharmind.helpers.BatchDbTest;
import pt.uminho.haslab.smpc.sharmind.helpers.ValuesGenerator;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class BinaryMultTest extends DoubleBatchValueProtocolTest {

    public BinaryMultTest(int nbits, List<BigInteger> firstValues,
                          List<BigInteger> secondValues) {
        super(nbits, firstValues, secondValues);
    }

    /* Overrides default */
    @Parameterized.Parameters
    public static Collection nbitsValues() {
        return ValuesGenerator.BinaryBatchValuesGenerator();
    }

    @Override
    public List<byte[]> runProtocol(List<byte[]> firstShares,
                                    List<byte[]> secondShares, Player player) {
        SharemindSecretFunctions ssf = new SharemindSecretFunctions(nbits);
        return ssf.mult(firstShares, secondShares, player);
    }

    @Override
    public void condition(BatchDbTest db1, BatchDbTest db2, BatchDbTest db3) {
        List<byte[]> db1Results = db1.getResult();
        List<byte[]> db2Results = db2.getResult();
        List<byte[]> db3Results = db3.getResult();

        assertEquals(db1Results.size(), db2Results.size());
        assertEquals(db2Results.size(), db3Results.size());
        for (int i = 0; i < db1Results.size(); i++) {

            BigInteger u1 = new BigInteger(db1Results.get(i));
            BigInteger u2 = new BigInteger(db2Results.get(i));
            BigInteger u3 = new BigInteger(db3Results.get(i));
            SharedSecret secret = new SharemindSharedSecret(nbits + 1, u1, u2,
                    u3);
            BigInteger firstValue = this.firstValues.get(i);
            BigInteger secondValue = this.secondValues.get(i);
            // System.out.println(secret.unshare());
            // System.out.println(firstValue.and(secondValue));
            assertEquals(secret.unshare().equals(firstValue.and(secondValue)),
                    true);
        }

    }

}
