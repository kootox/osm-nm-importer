package com.github.kootox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nantes Metropole Culture sites importer
 *
 */
public class App
{
    //Sites gathered by city for exports
    Map<String,CultureSite> sitesPerCity = new HashMap<String,CultureSite>();


    public static void main( String[] args ) throws IOException {
        //************************* IMPORT DATA ***************************
        String input = args[0];
        File inputFile = new File(input);
        FileInputStream inputStream = new FileInputStream(inputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"ISO-8859-1"));

        String strLine;

        //Read header
        strLine = reader.readLine();

        List<CultureSite> sites = new ArrayList<CultureSite>();

        //Read File Line By Line
        while ((strLine = reader.readLine()) != null)   {

            //split columns
            String[] items = strLine.split(";");

            //split number and street
            String addressRegexp = "([0-9]*)\\s[^\\d]+";
            Pattern p = Pattern.compile(addressRegexp);
            String address = "";
            String number = "";
            String fullAddress = items[10];
            if (!"".equals(fullAddress)) {
                Matcher m = p.matcher(fullAddress);
                if (m.matches()) {
                    address = fullAddress.substring(fullAddress.indexOf(" ")+1);
                    number = fullAddress.substring(0, fullAddress.indexOf(" "));
                } else {
                    address = fullAddress;
                }
            }


            //import site
            CultureSite site = new CultureSite();
            site.setObjId(intValue(items[0]));
            site.setName(items[1]);
            site.setCategory(intValue(items[4]));
            site.setLib_category(items[5]);
            site.setType(intValue(items[6]));
            site.setLib_type(items[7]);
            site.setStatus(items[8]);
            site.setCity(items[9]);
            site.setStreet(address);
            site.setNumber(number);
            site.setPhone(items[11]);
            site.setWeb(items[12]);
            site.setPostalCode(intValue(items[13]));
            site.setLongitude(doubleValue(items[14]));
            site.setLatitude(doubleValue(items[15]));

            //Add site to list
            sites.add(site);
        }
        //Close the input stream
        inputStream.close();


        //************************* EXPORT TO OSM *************************

        // Create file
        String output = args[1];
        FileWriter fstream = new FileWriter(output);
        BufferedWriter out = new BufferedWriter(fstream);

        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<osm version=\"0.6\" generator=\"CGImap 0.0.2\">\n");

        int id = 1;

        for (CultureSite site :sites) {
            //node info
            builder.append("  <node ");
            builder.append("lat = \"")
                   .append(site.getLatitude())
                   .append("\" ");
            builder.append("lon=\"")
                   .append(site.getLongitude())
                   .append("\" ");
            builder.append("version=\"1\" ");
            builder.append("id=\"-")
                   .append(id)
                   .append("\">\n");

            //increment id
            id++;

            //tag values
            appendTagValue(builder, "source","Nantes Métropole 11/2011");
            appendTagValue(builder, "name",site.getName());
            if (!"".equals(site.getCity())){
                appendTagValue(builder, "addr:city", site.getCity());
            }
            if (!"".equals(site.getStreet())){
                appendTagValue(builder, "addr:street", site.getStreet());
            }
            if (!"".equals(site.getNumber())){
                appendTagValue(builder, "addr:number", site.getNumber());
            }
            if (!(site.getPostalCode() == 0)){
                appendTagValue(builder, "addr:postcode", site.getPostalCode());
            }
            if (!"".equals(site.getPhone())){
                appendTagValue(builder, "contact:phone", site.getPhone());
            }

            //type tag value
            if (site.getCategory() == 106) {
                appendTagValue(builder, "tourism", "gallery");
            }

            if (site.getCategory() == 103) {
                if (site.getType() == 10301 || site.getType() == 10303) {
                    appendTagValue(builder, "amenity", "library");
                }

                if (site.getType() == 10302) {
                    appendTagValue(builder, "amenity", "toy_library");
                }
            }

            if (site.getCategory() == 101) {
                if (site.getType() == 10101) {
                    appendTagValue(builder, "historic", "castle");
                }

                if (site.getType() == 10102) {
                    appendTagValue(builder, "tourism", "museum");
                }
            }

            if (site.getCategory() == 104) {
                appendTagValue(builder, "historic", "yes");
            }

            if (site.getCategory() == 107) {
                appendTagValue(builder, "amenity", "theatre");
            }

            if (site.getCategory() == 105) {
                appendTagValue(builder, "leisure", "art_school");
            }

            if (site.getCategory() == 108) {
                appendTagValue(builder, "amenity", "cinema");
            }
            
            //close node
            builder.append("  </node>\n");
        }

        builder.append("</osm>");

        out.write(builder.toString());
        
        //Close the output stream
        out.close();
    }

    /**
     * Method that extract the integer value of Strings like 1422,00000000
     * @param stringValue the string to parse
     * @return the int value
     */
    protected static int intValue(String stringValue) {
        int intValue = 0;
        int index = stringValue.indexOf(",");
        if (index != -1) {
            String intString = stringValue.substring(0,index);
            if (intString != null) {
                intValue = Integer.parseInt(intString);
            }
        }
        return intValue;
    }

    protected static double doubleValue(String stringValue) {
        String changedValue = stringValue.replaceAll(",", ".");
        double doubleValue = 0.0;

        if (!changedValue.isEmpty()) {
            doubleValue = Double.valueOf(changedValue);
        }

        return doubleValue;
    }

    protected static void appendTagValue (StringBuilder builder, String tag, Object value) {
        builder.append("    <tag k=\"")
               .append(tag)
               .append("\" v=\"")
               .append(value)
               .append("\"/>\n");
    }
}
