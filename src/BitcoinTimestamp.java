package fireduck64.bitcoin.timestamp;

import java.math.BigInteger;

import java.util.TreeSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.io.FileInputStream;
import java.io.File;

import java.security.MessageDigest;
import java.net.URL;

import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;

public class BitcoinTimestamp
{
    public static void main(String args[]) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Syntax: ");
            System.out.println("BitcointTimestamp encode HASH_STRING");
            System.out.println("  The hash string is expected to be a hexadecimal encoded output 256 bits in length");
            System.out.println("  Example:");
            System.out.println("    encode 5a85dc07abe09caf05b56603068d7255fa59737aacac7e959b7e7a544d322a9b");
            System.out.println("  Note: if you create a file 'hashcapture.list' it will be used for the destination addresses.");
            System.out.println("   if you don't do that, this program will use addresses of the author and your pitance will");
            System.out.println("   go to him.  It should be on average 0.006 BTC + transaction fees.");
            System.out.println("   Your file must contain at least 17 addresses, whitespace delimited.");
            System.out.println();
            System.out.println("BitcoinTimestamp decode TRANSACTION_ID");
            System.out.println("  Example:");
            System.out.println("    decode f6f89da0b22ca49233197e072a39554147b55755be0c7cdf139ad33cc973ec46");
            return;
        }
        String mode = args[0];
        String param = args[1];
        if (mode.equals("encode"))
        {
            System.out.println(getSendmanyCommand(param, loadAddressList()));
        }
        else if (mode.equals("decode"))
        {
            System.out.println(decodeHash(param));
        }
        else
        {
            System.out.println("Unknown mode: " + mode);
        }
    }

    public static TreeSet<String> loadAddressList()
        throws Exception
	{
        TreeSet<String> addresses = new TreeSet<String>();
        File f = new File("hashcapture.list");
        if (f.exists())
        {
            Scanner a_scan = new Scanner(new FileInputStream(f));
            while(a_scan.hasNext())
            {
                String addr = a_scan.next().trim();

                if (!addr.isEmpty())
                {
                    addresses.add(addr);
                }
            }
            
        }
        else
        {
            /**
             * Use the author's addresses in case the user hasn't define any.
             *
             * Exactly how crooked this is a matter of perspective.
             * The most this could sent is 0.01114112 and that would be if the 
             * hash was all FFs.  Average should be around 0.006 BTC.
             *
             * I call it a tip to the author by the altruistic or lazy
             */
            addresses.add("115LLGFjCeuR43kqPut3VgQaijykg4ZUvu");
            addresses.add("12eAxYyqotjCFrXctCFpDGszgcsEYgVztr");
            addresses.add("13s99X5GxKjZDnZCEMD4QwFbGgqGFAwmzJ");
            addresses.add("13yBeZvwoXFMxbmqScnLqKQipFzRkqVxPD");
            addresses.add("14Mx7oTvvBnSKn2dj6NjtbqBJ5oNYqurq8");
            addresses.add("14ZsLnXHbCttAxo4izUUyeZbqETArvSMGp");
            addresses.add("162jZXDsnwU61t8iBTS2MaMrYPM25CRC8W");
            addresses.add("16RNY4P3g8pQ3nyVZVuvC4bxSo46abpaJN");
            addresses.add("18b4dBWzDsUfYoTfFnPTGk8brryh2VRBkp");
            addresses.add("19M9tTY4yoFLt2kfKyHhG4zcviDcYJ1TgT");
            addresses.add("1Aa1R7bvDYcQRNsAcQ1qsr7nUXAiDpnYcD");
            addresses.add("1B1U5HvHCVPUxdv2BzfdrmeGXW1TLFf6qM");
            addresses.add("1B9zumkrpGaHQ4HbWjDP6DAVVr4FKffR59");
            addresses.add("1C3BMYos5ts23WW1N9eVwGuAXFUpVRyDEG");
            addresses.add("1CoVCczCw7R5nzV45uw4FbWAESktUJfZnj");
            addresses.add("1CZcY7eZFDx7DBqsG9bBe9h4HgPx5Zhfur");
            addresses.add("1D2uRc1B59FHyMPA2Ncwfe4wJo42csDWVk");
            
        }
        return addresses;

	}

    public static String getSendmanyCommand(String hash, TreeSet<String> addresses) throws Exception
    {
        TreeMap<String, String> hash160_to_address=new TreeMap<String, String>();
        TreeSet<String> hash160_set = new TreeSet<String>();

        //We have to sort by the same thing we are going to sort by on the other side
        //the hash160 value for the key is a good one
        for(String address : addresses)
        {
            String hash160 = getHash160FromAddress(address);
            hash160_set.add(hash160);
            hash160_to_address.put(hash160, address);

        }
            String token = hash;

            BigInteger bi = new BigInteger(token, 16);

            BigInteger mod = new BigInteger("65536");
            DecimalFormat df = new DecimalFormat("0.00000000");

            LinkedList<BigInteger> amount_values = new LinkedList<BigInteger>();
            for(int i=0; i<16; i++)
            {
                BigInteger b = bi.mod(mod);
                amount_values.addFirst(b);
                bi = bi.divide(mod);

            }

            double btc_factor = 0.00000001f;

            amount_values.addLast(getChecksum16bits(token));

            StringBuilder sb = new StringBuilder();
            sb.append("sendmany");
            sb.append(" fromacct");
            sb.append(" '{");

            Iterator<String> hash_i = hash160_set.iterator();

            boolean first=true;

            for(BigInteger amt : amount_values)
            {
             
                String hash160 = hash_i.next();
                String address = hash160_to_address.get(hash160);
                int val = amt.intValue();
                double d = btc_factor * val;

                if (!first)
                    sb.append(",");
                sb.append('"');
                sb.append(address);
                sb.append('"');
                sb.append(":");
                sb.append(df.format(d));

                first=false;
            }
           
            sb.append("}'");
            return sb.toString();

    }

    public static BigInteger getChecksum16bits(String token)
        throws Exception
    {
            
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte b[] = md.digest(token.getBytes("UTF-8"));

        BigInteger bi = new BigInteger(1,b);

        BigInteger mod = new BigInteger("65536");

        BigInteger ret = bi.mod(mod);
        return ret;
            
    }

    /**
     * Returns the set of possible hashes if any encoded within the following transaction ID.
     *
     * If the list is empty, it means something is wrong and no hash could be found.
     * If there are multiple entries, it means that there was a change block that somehow
     * made the checksum still work (should be 1 in every 65536 when the change is smaller than 65536).
     * However, in that unlikely event then you just have two strings and your hash is one of them.
     *
     * Pulls the raw transaction data from blockexploer.
     */
    public static Set<String> decodeHash(String transaction_id)
        throws Exception
    {
        String url = "http://blockexplorer.com/rawtx/" + transaction_id;
        JSONObject tx_obj = readUrl(url);

        JSONArray out_array = tx_obj.getJSONArray("out");
        int len = out_array.length();

        TreeMap<String, Long> payment_amounts = new TreeMap<String, Long>();

        for(int i=0; i<len; i++)
        {
            JSONObject out_entry = out_array.getJSONObject(i);
            double value = out_entry.getDouble("value");
            long long_value = (long)Math.round(value * 100000000L);
            String account = out_entry.getString("scriptPubKey");

            if (long_value < 65536)
            {
                payment_amounts.put(account, long_value);
            }
        }
        
        ArrayList<String> chunks = new ArrayList<String>();
        for(long n : payment_amounts.values())
        {
            BigInteger bi = new BigInteger("" + n);
            String hex = bi.toString(16);
            while(hex.length() < 4)
            {
                hex = "0" + hex;
            }
            chunks.add(hex);
        }
        Set<String> ts = new TreeSet<String>();
        if (chunks.size() == 17)
        {
            String s = tryDecode(chunks);
            if (s != null)
            {
                ts.add(s);
            }
        }
        if (chunks.size() > 17)
        {
            TreeSet<Integer> avoid_list = new TreeSet<Integer>();
            ts = recursiveChunkAssemble(chunks, 0, avoid_list);

        }
        return ts;

    }


    public static Set<String> recursiveChunkAssemble(ArrayList<String> chunks, int idx, TreeSet<Integer> avoid_list)
        throws Exception
    {
        TreeSet<String> found = new TreeSet<String>();
        if (idx == chunks.size())
        {
            if (chunks.size() - avoid_list.size() == 17)
            {
                String s = tryDecode(chunks, avoid_list);
                if (s != null) found.add(s);
            }

        }
        else
        {
            if (chunks.size() - avoid_list.size() > 17)
            {
                avoid_list.add(idx);
                found.addAll(recursiveChunkAssemble(chunks, idx+1, avoid_list));
                avoid_list.remove(idx);
            }
            found.addAll(recursiveChunkAssemble(chunks, idx+1, avoid_list));
        }

        return found;

    }

    public static String tryDecode(ArrayList<String> chunks)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        String check_chunk = null;
        for(int i=0; i<16; i++)
        {
            sb.append(chunks.get(i));
        }
        String found_checksum = chunks.get(16);
        String calc_checksum = getChecksum16bits(sb.toString()).toString(16);
        if (found_checksum.equals(calc_checksum))
        {
            return sb.toString();
        }        
        return null;
    }
    public static String tryDecode(ArrayList<String> chunks, TreeSet<Integer> avoid_list)
        throws Exception
    {
        ArrayList<String> active_list = new ArrayList<String>();
        for(int i=0; i<chunks.size(); i++)
        {
            if (!avoid_list.contains(i))
            {
                active_list.add(chunks.get(i));
            }
        }
        return tryDecode(active_list);

    }

    public static JSONObject readUrl(String url)
        throws Exception
    {
        URL u = new URL(url);
        return new JSONObject(new JSONTokener(u.openConnection().getInputStream()));

    }

    public static String getHash160FromAddress(String address)
    {
        String base58="123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        TreeMap<Character, Integer> radix_value_map = new TreeMap<Character, Integer>();

        for(int i=0; i<base58.length(); i++)
        {
            radix_value_map.put(base58.charAt(i), i);
        }



        BigInteger bi = BigInteger.ZERO;
        BigInteger base = new BigInteger("58");

        for(int i=0; i<address.length(); i++)
        {
            char x = address.charAt(i);
            int val = radix_value_map.get(x);
            bi = bi.multiply(base);
            bi = bi.add(BigInteger.valueOf(val));

        }

        String hex= bi.toString(16);
        while(hex.length() < 48)
        {
            hex = "0" + hex;
        }

        String hash160=hex.substring(0, 40);

        return hash160;

    }

}
