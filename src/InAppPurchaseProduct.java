import java.util.List;

public class InAppPurchaseProduct {
    private String productId;
    private String referenceName;
    private String type;
    private List<Product> products;
    private List<Locale> locales;
    private ReviewScreenshot reviewScreenshot;
    private String reviewNotes;

    public InAppPurchaseProduct(String productId, String referenceName, String type, List<Product> products, List<Locale> locales, ReviewScreenshot reviewScreenshot, String reviewNotes) {
        this.productId = productId;
        this.referenceName = referenceName;
        this.type = type;
        this.products = products;
        this.locales = locales;
        this.reviewScreenshot = reviewScreenshot;
        this.reviewNotes = reviewNotes;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    public ReviewScreenshot getReviewScreenshot() {
        return reviewScreenshot;
    }

    public void setReviewScreenshot(ReviewScreenshot reviewScreenshot) {
        this.reviewScreenshot = reviewScreenshot;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    static class Product {
        private boolean clearedForSale;
        private int wholesalePriceTier;

        public Product() {
        }
        public Product(boolean clearedForSale, int wholesalePriceTier) {
            this.clearedForSale = clearedForSale;
            this.wholesalePriceTier = wholesalePriceTier;
        }

        public boolean isClearedForSale() {
            return clearedForSale;
        }

        public void setClearedForSale(boolean clearedForSale) {
            this.clearedForSale = clearedForSale;
        }

        public int getWholesalePriceTier() {
            return wholesalePriceTier;
        }

        public void setWholesalePriceTier(int wholesalePriceTier) {
            this.wholesalePriceTier = wholesalePriceTier;
        }
    }
    static class Locale {
        private String name;
        private String title;
        private String description;

        public Locale() {}

        public Locale(String name, String title, String description) {
            this.name = name;
            this.title = title;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    static class ReviewScreenshot {
        private String file_name;
        private int size;
        private String checksum;
        private final String checksumType = "md5";

        public ReviewScreenshot() {
        }

        public ReviewScreenshot(String file_name, int size, String checksum) {
            this.file_name = file_name;
            this.size = size;
            this.checksum = checksum;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public String getChecksumType() {
            return checksumType;
        }
    }


}
