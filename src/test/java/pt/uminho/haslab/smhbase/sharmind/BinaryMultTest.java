package pt.uminho.haslab.smhbase.sharmind;

import java.math.BigInteger;
import java.util.Collection;
import static junit.framework.TestCase.assertEquals;
import org.junit.runners.Parameterized;
import pt.uminho.haslab.smhbase.interfaces.Secret;
import pt.uminho.haslab.smhbase.interfaces.SharedSecret;
import pt.uminho.haslab.smhbase.sharemindImp.SharemindSecret;
import pt.uminho.haslab.smhbase.sharemindImp.SharemindSharedSecret;
import pt.uminho.haslab.smhbase.sharmind.helpers.DbTest;
import pt.uminho.haslab.smhbase.sharmind.helpers.ValuesGenerator;

public class BinaryMultTest extends DoubleValueProtocolTest {

	public BinaryMultTest(int nbits, BigInteger value1, BigInteger value2) {
		super(nbits, value1, value2);
	}

	/* Overrides default */
	@Parameterized.Parameters
	public static Collection nbitsValues() {
		return ValuesGenerator.BinaryValuesGenerator();
	}

	@Override
	public Secret runProtocol(Secret firstSecret, Secret secondSecret) {
		return ((SharemindSecret) firstSecret)
				.mult((SharemindSecret) secondSecret);
	}

	@Override
	public void condition(DbTest db1, DbTest db2, DbTest db3) {
		BigInteger u1 = ((SharemindSecret) db1.getResult()).getValue();
		BigInteger u2 = ((SharemindSecret) db2.getResult()).getValue();
		BigInteger u3 = ((SharemindSecret) db3.getResult()).getValue();;
		SharedSecret secret = new SharemindSharedSecret(nbits + 1, u1, u2, u3);
		assertEquals(
				secret.unshare().equals(this.firstValue.and(this.secondValue)),
				true);
	}
}
