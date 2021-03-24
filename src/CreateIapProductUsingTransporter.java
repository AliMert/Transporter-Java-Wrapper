import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;


public class CreateIapProductUsingTransporter {

    public static final String appleId = Constants.appleId;
    public static final String appSpecificPassword = Constants.appSpecificPassword;
    public static final String vendorId = Constants.appSpecificPassword;

    private static final String itmspDestination = Constants.itmspDestination;
    public static final String xmlFilePath = Constants.xmlFilePath;
    public static final String screenShotFileName = Constants.screenShotFileName;
    public static final String copyFromScreenShotFilePath = Constants.copyFromScreenShotFilePath;
    public static final String copyToScreenShotFilePath = Constants.copyToScreenShotFilePath;

    static String retrieveCommandString = "xcrun iTMSTransporter -m lookupMetadata -u "+appleId+" -p "+appSpecificPassword+" -vendor_id "+vendorId+" -destination "+itmspDestination;
    static String verifyCommandString = "xcrun iTMSTransporter -m verify -u "+ appleId+" -p "+appSpecificPassword+" -f "+itmspDestination+vendorId+".itmsp";
    static String uploadCommandString = "xcrun iTMSTransporter -m upload -u "+appleId+" -p "+appSpecificPassword+" -f "+itmspDestination+vendorId+".itmsp";


    public static void main(String[] args) {
        int fileSize = 0;
        String checksum = "";
        String[] errorReport;
        Command command;

        //
        // 1. get app metadata
        //
        System.out.println("\nLooking up metadata");
        command = new Command(retrieveCommandString) ;
        errorReport = command.run();
        if (errorReport != null) {
            System.out.println("\n\nError Report: \n");
            for (String l : errorReport)
                System.out.println(l);
            System.exit(-1);
        }
        System.out.println("\nmetadata is retrieved.\n");
        //
        // add new in-app product to metadata
        // 2. add screenshot to itmsp
        //
        try {
            addScreenshotToItmsp();
            File file = new File(copyFromScreenShotFilePath);
            if (!file.exists() || !file.isFile()) return;

            fileSize = getFileSizeBytes(file);

            checksum = checksum(file);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nProblem occurred while copying screenshot\n");
            System.exit(-1);
        }
        System.out.println("\nphoto is copied to metadata folder\n");


        List<InAppPurchaseProduct.Product> productList = new ArrayList<>();
        productList.add(new InAppPurchaseProduct.Product(true, 5));

        List<InAppPurchaseProduct.Locale> localeList = new ArrayList<>();
        localeList.add(new InAppPurchaseProduct.Locale("en-US", "test 5000 Diamonds","testGet 5000 Diamonds"));

        InAppPurchaseProduct.ReviewScreenshot reviewScreenshot = new InAppPurchaseProduct.ReviewScreenshot(screenShotFileName, fileSize, checksum);


        InAppPurchaseProduct inAppPurchaseProduct = new InAppPurchaseProduct("test1234iddd", "tttest 5000 Diamonds", "consumable", productList, localeList, reviewScreenshot, "12 Some notes for the reviewer.");
        //InAppPurchase inAppPurchase2 = new InAppPurchase("asdfs2", "asdfs2 Test - 500 Diamonds", "consumable", productList, localeList, reviewScreenshot, "Some notes for the reviewer.");

        System.out.println("\nediting metadata...\n");
        //
        // 3. edit metadata
        //
        try {
            addInAppPurchaseToMetadata(inAppPurchaseProduct);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem occurred while appending product to metadata");
            System.exit(-1);
        }
        System.out.println("\nmetadata is edited\n");
        //
        // 4. verify metadata
        //
        System.out.println("\nverifying metadata...\n");
        command = new Command(verifyCommandString);
        errorReport = command.run();
        if (errorReport != null) {
            System.out.println("\n\nError Report: \n");
            for (String l : errorReport)
                System.out.println(l);
            System.exit(-1);
        }
        System.out.println("\nmetadata is verified.\n");
        //
        // 5. upload metadata
        //
        System.out.println("\nuploading metadata...\n");
        command = new Command(uploadCommandString) ;
        command.run();
        System.out.println("\nmetadata is uploaded.\n");

        //
        // 6. delete copied screenshot and retrieved metadata (itmsp)
        //
    }

    public static void addScreenshotToItmsp() throws Exception {
        Path copyTo = Paths.get(copyToScreenShotFilePath);
        Path copyFrom = Paths.get(copyFromScreenShotFilePath);
        Files.copy(copyFrom, copyTo, StandardCopyOption.REPLACE_EXISTING);

    }

    static void addInAppPurchaseToMetadata(InAppPurchaseProduct inAppPurchaseProduct) throws Exception {
        String inAppPurchasesTag = "in_app_purchases";
        String inAppPurchasesParentTag = "software_metadata";
        String inAppPurchaseTag = "in_app_purchase";
        String productIdTag = "product_id";
        String referenceNameTag = "reference_name";
        String typeTag = "type";


        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(xmlFilePath);
        Element root = document.getDocumentElement();

        Node inAppPurchases = document.getElementsByTagName(inAppPurchasesTag).item(0);
        if (inAppPurchases == null) {
            Node inAppPurchasesParent = document.getElementsByTagName(inAppPurchasesParentTag).item(0);
            inAppPurchases = document.createElement(inAppPurchasesTag);
            inAppPurchasesParent.appendChild(inAppPurchases);
        } else if (!inAppPurchases.hasChildNodes()) {
            System.out.println("Gotcha");
        }

        Element newProduct = document.createElement(inAppPurchaseTag);

        // productId
        Element productId = document.createElement(productIdTag);
        productId.appendChild(document.createTextNode(inAppPurchaseProduct.getProductId()));
        newProduct.appendChild(productId);

        // referenceName
        Element referenceName = document.createElement(referenceNameTag);
        referenceName.appendChild(document.createTextNode(inAppPurchaseProduct.getReferenceName()));
        newProduct.appendChild(referenceName);

        // type
        Element type = document.createElement("type");
        type.appendChild(document.createTextNode(inAppPurchaseProduct.getType()));
        newProduct.appendChild(type);

        // products
        Element products = document.createElement("products");
        for (InAppPurchaseProduct.Product p : inAppPurchaseProduct.getProducts()) {
            Element product = document.createElement("product");

            Element cleared_for_sale = document.createElement("cleared_for_sale");
            cleared_for_sale.appendChild(document.createTextNode(Boolean.toString(p.isClearedForSale())));
            product.appendChild(cleared_for_sale);
            Element wholesale_price_tier = document.createElement("wholesale_price_tier");
            wholesale_price_tier.appendChild(document.createTextNode(Integer.toString(p.getWholesalePriceTier())));

            product.appendChild(wholesale_price_tier);
            products.appendChild(product);
        }
        newProduct.appendChild(products);

        // locales
        Element locales = document.createElement("locales");
        for (InAppPurchaseProduct.Locale l : inAppPurchaseProduct.getLocales()) {
            Element locale = document.createElement("locale");
            locale.setAttribute("name", l.getName());

            Element title = document.createElement("title");
            title.appendChild(document.createTextNode(l.getTitle()));
            locale.appendChild(title);
            Element description = document.createElement("description");
            description.appendChild(document.createTextNode(l.getDescription()));

            locale.appendChild(description);
            locales.appendChild(locale);
        }
        newProduct.appendChild(locales);

        // review_screenshot
        Element review_screenshot = document.createElement("review_screenshot");

        Element file_name = document.createElement("file_name");
        file_name.appendChild(document.createTextNode(inAppPurchaseProduct.getReviewScreenshot().getFile_name()));
        review_screenshot.appendChild(file_name);

        Element size = document.createElement("size");
        size.appendChild(document.createTextNode(Integer.toString(inAppPurchaseProduct.getReviewScreenshot().getSize())));
        review_screenshot.appendChild(size);

        Element checksum = document.createElement("checksum");
        checksum.setAttribute("type", inAppPurchaseProduct.getReviewScreenshot().getChecksumType());


        checksum.appendChild(document.createTextNode(inAppPurchaseProduct.getReviewScreenshot().getChecksum()));
        review_screenshot.appendChild(checksum);

        newProduct.appendChild(review_screenshot);

        // review_notes
        Element review_notes = document.createElement("review_notes");
        review_notes.appendChild(document.createTextNode(inAppPurchaseProduct.getReviewNotes()));
        newProduct.appendChild(review_notes);

        inAppPurchases.appendChild(newProduct);

        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(new File(xmlFilePath));
        transformer.transform(source, result);
    }

    public static String checksum(File file) {
        try {
            InputStream fin = new FileInputStream(file);
            java.security.MessageDigest md5er =
                    MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int read;
            do {
                read = fin.read(buffer);
                if (read > 0)
                    md5er.update(buffer, 0, read);
            } while (read != -1);
            fin.close();
            byte[] digest = md5er.digest();
            if (digest == null)
                return null;
            StringBuilder strDigest = new StringBuilder();
            for (byte b : digest) {
                strDigest.append(Integer.toString((b & 0xff)
                        + 0x100, 16).substring(1).toLowerCase());
            }
            return strDigest.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static int getFileSizeBytes(File file) {
        return (int) file.length();
    }
}