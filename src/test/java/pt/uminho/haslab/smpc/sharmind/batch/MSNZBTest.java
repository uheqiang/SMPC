package pt.uminho.haslab.smpc.sharmind.batch;

import pt.uminho.haslab.smpc.exceptions.InvalidNumberOfBits;
import pt.uminho.haslab.smpc.exceptions.InvalidSecretValue;
import pt.uminho.haslab.smpc.interfaces.Dealer;
import pt.uminho.haslab.smpc.interfaces.Player;
import pt.uminho.haslab.smpc.interfaces.Players;
import pt.uminho.haslab.smpc.interfaces.SharedSecret;
import pt.uminho.haslab.smpc.sharemindImp.BigInteger.SharemindBitVectorDealer;
import pt.uminho.haslab.smpc.sharemindImp.BigInteger.SharemindBitVectorSharedSecret;
import pt.uminho.haslab.smpc.sharemindImp.BigInteger.SharemindSecretFunctions;
import pt.uminho.haslab.smpc.sharmind.helpers.BatchDbTest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class MSNZBTest extends SingleBatchValueProtocolTest {

    public MSNZBTest(int nbits, List<BigInteger> values) {
        super(nbits, values);
    }

    @Override
    public List<BatchDbTest> prepareDatabases(Players players)
            throws InvalidNumberOfBits, InvalidSecretValue {

        List<byte[]> sbv1s = new ArrayList<byte[]>();
        List<byte[]> sbv2s = new ArrayList<byte[]>();
        List<byte[]> sbv3s = new ArrayList<byte[]>();
        Player p0 = players.getPlayer(0);
        Player p1 = players.getPlayer(1);
        Player p2 = players.getPlayer(2);

        for (BigInteger val : this.values) {
            BigInteger u = val;
            Dealer dealer = new SharemindBitVectorDealer(nbits);
            SharemindBitVectorSharedSecret secret = (SharemindBitVectorSharedSecret) dealer
                    .share(u);
            BigInteger sbv1 = secret.getU1();
            BigInteger sbv2 = secret.getU2();
            BigInteger sbv3 = secret.getU3();

            sbv1s.add(sbv1.toByteArray());
            sbv2s.add(sbv2.toByteArray());
            sbv3s.add(sbv3.toByteArray());
        }

		/* DB is a class of SingleValueProtocolTest */
        BatchDbTest rdb0 = new Db(sbv1s, p0);
        BatchDbTest rdb1 = new Db(sbv2s, p1);
        BatchDbTest rdb2 = new Db(sbv3s, p2);

        List<BatchDbTest> result = new ArrayList<BatchDbTest>();

        result.add(rdb0);
        result.add(rdb1);
        result.add(rdb2);
        return result;
    }

    private BigInteger oracle(BigInteger value) {
        /*
		 * This function goes throught the bits from the end to the start and
		 * identifies the most significant bit. After that it generates a bit
		 * string with ones that has the same size as the original value.
		 */

        boolean foundMSB = false;
        StringBuilder sbt = new StringBuilder();

        for (int i = nbits - 1; i > -1; i--) {

            if (!foundMSB) {

                foundMSB = value.testBit(i);
                sbt.append(foundMSB ? 1 : 0);

            } else {
                sbt.append(0);
            }

        }
        if (sbt.toString().isEmpty()) {
            return BigInteger.ZERO;
        }
        return new BigInteger(sbt.toString(), 2);
    }

    @Override
    public List<byte[]> runProtocol(List<byte[]> shares, Player player) {
        BigInteger bitMod = BigInteger.valueOf(2).pow(nbits);

        SharemindSecretFunctions ssf = new SharemindSecretFunctions(nbits,
                bitMod);
        return ssf.msnzb(shares, player);
    }

    @Override
    public void condition(BatchDbTest db1, BatchDbTest db2, BatchDbTest db3) {
        List<byte[]> u1s = db1.getResult();
        List<byte[]> u2s = db2.getResult();
        List<byte[]> u3s = db3.getResult();

        for (int i = 0; i < u1s.size(); i++) {
            BigInteger u1 = new BigInteger(u1s.get(i));
            BigInteger u2 = new BigInteger(u2s.get(i));
            BigInteger u3 = new BigInteger(u3s.get(i));
            BigInteger value = values.get(i);
            System.out.println("Val 0 " + Integer.toBinaryString(u1.intValue()));
            System.out.println("Val 1 " + Integer.toBinaryString(u2.intValue()));
            System.out.println("Val 2 " + Integer.toBinaryString(u3.intValue()));

            SharedSecret secret = new SharemindBitVectorSharedSecret(nbits, u1,
                    u2, u3);
            BigInteger result = secret.unshare();
            System.out.println("Input value is " + value);
            int expected = oracle(value).intValue();
            System.out.println("Expected result is " + expected + " <-> "+ Integer.toBinaryString(expected));
            System.out.println("Result is  " + result + " <-> " + Integer.toBinaryString(result.intValue()));
            assertEquals(oracle(value), result);
        }
    }

}
