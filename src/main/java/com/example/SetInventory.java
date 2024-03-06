package com.example;

import com.google.cloud.retail.v2.CustomAttribute;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.ProductName;
import com.google.cloud.retail.v2.ProductServiceClient;
import com.google.cloud.retail.v2.SetInventoryRequest;
import com.google.longrunning.Operation;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;

public class SetInventory {
    public static final String ATTRIBUTE_TO_CHANGE = "${ATTRIBUTE_TO_CHANGE}";
    public static final String DEFAULT_CATALOG = "${DEFAULT_CATALOG}";
    public static final String DEFAULT_BRANCH = "${DEFAULT_BRANCH}";
    public static final String LOCATION = "${LOCATION}";
    public static final String PROJECT = "${PROJECT}";

    public static void main(String... args) throws Exception {
                String productId = "";

        try (ProductServiceClient productServiceClient = ProductServiceClient.create()) {
            // Fails with - Caused by: io.grpc.StatusRuntimeException: FAILED_PRECONDITION: Cannot write to path 'attributes'
            partialUpdateAttempt(productId, productServiceClient);
        }
    }

    private static void partialUpdateAttempt(String productId, ProductServiceClient productServiceClient) {
        try {
            FieldMask mask = attributesFieldMask();
            SetInventoryRequest request = SetInventoryRequest.newBuilder()
                    .setInventory(getProduct(productId))
                    .setSetMask(mask)
                    .setSetTime(Timestamp.newBuilder().build())
                    .setAllowMissing(false)
                    .build();
            Operation response = productServiceClient.setInventoryCallable().call(request);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Product getProduct(String productId) {
        CustomAttribute value = CustomAttribute.newBuilder().addText(Boolean.FALSE.toString()).build();
        return Product.newBuilder()
                .setName(toProductName(productId).toString())
                .putAttributes(ATTRIBUTE_TO_CHANGE, value)
                .build();
    }

    private static FieldMask attributesFieldMask() {
        return FieldMask.newBuilder()
                .addPaths(Product.getDescriptor().findFieldByNumber(Product.ATTRIBUTES_FIELD_NUMBER).getName()).build();
    }

    private static ProductName toProductName(String productId) {
        return ProductName.newBuilder()
                .setProduct(productId)
                .setCatalog(DEFAULT_CATALOG)
                .setBranch(DEFAULT_BRANCH)
                .setLocation(LOCATION)
                .setProject(PROJECT)
                .build();
    }
}
