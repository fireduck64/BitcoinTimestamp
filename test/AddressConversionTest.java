

import org.junit.Assert;
import org.junit.Test;

import fireduck64.bitcoin.timestamp.BitcoinTimestamp;

public class AddressConversionTest
{
    @Test
    public void testAddressToHash160()
    {
        
        //Values from blockexplorer
        //
        Assert.assertEquals("0903a4e4cd1902a5064adecea790217c8cba3bde", 
            BitcoinTimestamp.getHash160FromAddress("1pfSvbhbjGdSabQ6fYm4dbKEFDuJW9xZw"));

        Assert.assertEquals("00d1cd2e5ec984226c98f85a69852ee9e6c659bd", 
            BitcoinTimestamp.getHash160FromAddress("115LLGFjCeuR43kqPut3VgQaijykg4ZUvu"));

        Assert.assertEquals("ebfac9d836423078b592d13a84ff3dad58602fe9", 
            BitcoinTimestamp.getHash160FromAddress("1NWkEAiWPLtakyh7DXSF9G8GN3ZzeaRNPr"));
    }
    

}
