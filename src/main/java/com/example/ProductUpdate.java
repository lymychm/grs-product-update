package com.example;

import com.google.cloud.retail.v2.CustomAttribute;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.ProductName;
import com.google.cloud.retail.v2.ProductServiceClient;
import com.google.cloud.retail.v2.UpdateProductRequest;
import com.google.protobuf.FieldMask;

public class ProductUpdate {
    public static final String ATTRIBUTE_TO_CHANGE = "${ATTRIBUTE_TO_CHANGE}";
    public static final String DEFAULT_CATALOG = "${DEFAULT_CATALOG}";
    public static final String DEFAULT_BRANCH = "${DEFAULT_BRANCH}";
    public static final String LOCATION = "${LOCATION}";
    public static final String PROJECT = "${PROJECT}";

    public static void main(String... args) throws Exception {
        String productId = "";
        try (ProductServiceClient productServiceClient = ProductServiceClient.create()) {
            // Attempt1 & Attempt2 fails either
            //  - with io.grpc.StatusRuntimeException: INVALID_ARGUMENT: Unsupported path 'productId' in 'product.retrievableFields'.
            //    or
            //  - with io.grpc.StatusRuntimeException: INVALID_ARGUMENT: Unsupported path 'rating.average_rating' in 'product.retrievableFields'.
            updateAttempt1(productId, productServiceClient);
            updateAttempt2(productId, productServiceClient);

            // Fails with: io.grpc.StatusRuntimeException: INVALID_ARGUMENT: Unsupported path 'rating.average_rating' in 'product.retrievableFields'.
            updateAttempt3(productId, productServiceClient);

            // Works after adding .addPaths(Product.getDescriptor().findFieldByNumber(Product.RETRIEVABLE_FIELDS_FIELD_NUMBER).getName())
            // ¯\_(ツ)_/¯
            updateAttempt4(productId, productServiceClient);
        }
    }

    private static void updateAttempt1(String productId, ProductServiceClient productServiceClient) {
        ProductName productName = toProductName(productId);
        Product product = productServiceClient.getProduct(productName);
        UpdateProductRequest request = UpdateProductRequest.newBuilder()
                .setAllowMissing(false)
                .setProduct(product.toBuilder().setName(toProductName(product.getId()).toString()).build())
                .build();
        Product result = productServiceClient.updateProduct(request);
        System.out.println(result);
    }

    private static void updateAttempt2(String productId, ProductServiceClient productServiceClient) {
        ProductName productName = toProductName(productId);
        CustomAttribute value = CustomAttribute.newBuilder().addText(Boolean.FALSE.toString()).build();
        Product product = productServiceClient.getProduct(productName);
        product = product.toBuilder().putAttributes(ATTRIBUTE_TO_CHANGE, value).build();
        UpdateProductRequest request = UpdateProductRequest.newBuilder()
                .setAllowMissing(false)
                .setProduct(product.toBuilder().setName(toProductName(product.getId()).toString()).build())
                .build();
        Product result = productServiceClient.updateProduct(request);
        System.out.println(result);
    }

    private static void updateAttempt3(String productId, ProductServiceClient productServiceClient) {
        Product product = getProduct(productId);
        UpdateProductRequest request = UpdateProductRequest.newBuilder()
                .setProduct(product.toBuilder().setName(toProductName(productId).toString()))
                .setAllowMissing(false)
                .setUpdateMask(notWorkingFieldMask())
                .build();
        Product result = productServiceClient.updateProduct(request);
        System.out.println(result);
    }

    private static void updateAttempt4(String productId, ProductServiceClient productServiceClient) {
        Product product = getProduct(productId);
        UpdateProductRequest request = UpdateProductRequest.newBuilder()
                .setProduct(product.toBuilder().setName(toProductName(productId).toString()))
                .setAllowMissing(false)
                .setUpdateMask(workingFieldMask())
                .build();
        Product result = productServiceClient.updateProduct(request);
        System.out.println(result);
    }

    private static Product getProduct(String productId) {
        Product.Builder builder = Product.newBuilder();
        builder.setId(productId);
        builder.putAttributes(ATTRIBUTE_TO_CHANGE, CustomAttribute.newBuilder().addText(Boolean.FALSE.toString()).build());
        return builder.setAvailability(Product.Availability.OUT_OF_STOCK).build();
    }

    private static ProductName toProductName(String id) {
        return ProductName.newBuilder().setProduct(id)
                .setCatalog(DEFAULT_CATALOG)
                .setBranch(DEFAULT_BRANCH)
                .setLocation(LOCATION)
                .setProject(PROJECT)
                .build();
    }

    private static FieldMask notWorkingFieldMask() {
        return FieldMask
                .newBuilder()
                .addPaths("attributes." + ATTRIBUTE_TO_CHANGE)
                .addPaths(Product.getDescriptor().findFieldByNumber(Product.AVAILABILITY_FIELD_NUMBER).getName())
                .build();
    }

    private static FieldMask workingFieldMask() {
        return FieldMask
                .newBuilder()
                .addPaths("attributes." + ATTRIBUTE_TO_CHANGE)
                .addPaths(Product.getDescriptor().findFieldByNumber(Product.AVAILABILITY_FIELD_NUMBER).getName())
                .addPaths(Product.getDescriptor().findFieldByNumber(Product.RETRIEVABLE_FIELDS_FIELD_NUMBER).getName())
                .build();
    }
}
