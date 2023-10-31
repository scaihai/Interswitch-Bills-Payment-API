# Interswitch Bills Payment API
This project demonstrates how to implement Interswitch Bills Payment API specification.

# Language
Though the program is written in Java programming language, familiarity with a similar high level language is sufficient to understand how the API specification works. The major framework used is Spring Boot.

# Code Examples
The API specification requires that your service exposes an endpoint that receives XML. This endpoint will handle two operations.

* Customer Validation: Fetching and returning the details of the customer
* Payment Notification: Giving the customer value for the payment they made

This means the XML should be inspected to determine if the required operation is **Customer Validation** or **Payment Notification**. The following snippet demonstrates how this can be achieved.

```java
	@PostMapping(value = "/isw-bills-payment", consumes = TEXT_XML_VALUE, produces = TEXT_XML_VALUE)
	private String payBills(@RequestBody String xml) {
		if (xml.contains("CustomerInformationRequest")) {
			return validateCustomerReference(xml);
		} else if (xml.contains("PaymentNotificationRequest")) {
			return notifyOfPayment(xml);
		} else {
			throw new RuntimeException("Invalid request");
		}
	}
```

## Customer Validation
When the XML contains 'CustomerInformationRequest' the implementation should fetch the details of the customer and return it in the response XML. The main tag in the **Customer Validation** request is 'CustReference'. Use the value of this tag to retrieve details of the customer, such as the customer's name (and address, if applicable). The exact details of the customer to be returned depends on what type of biller you are. Sample customer validation payloads are show below, with implementation.

### Sample Customer Valiation Request
```
<CustomerInformationRequest>
    <MerchantReference>3527</MerchantReference>
    <CustReference>0123</CustReference>
    <ServiceUsername />
    <ServicePassword />
    <FtpUsername />
    <FtpPassword />
</CustomerInformationRequest>
```

### Sample Customer Validation Response
```
<CustomerInformationResponse>
    <Customers>
        <Customer>
            <Status>0</Status>
            <CustReference>0123</CustReference>
            <FirstName>John</FirstName>
            <LastName>Doe</LastName>
            <OtherName>Doe</OtherName>
            <Email>example@mail.com</Email>
            <Phone>08012345678</Phone>
            <StatusMessage>Successful</StatusMessage>
        </Customer>
    </Customers>
</CustomerInformationResponse>
```

### Customer Validation Implementation Snippet
```
	private String validateCustomerReference(String xml) {
		CustomerInformationRequest request = xmlToObject(xml, CustomerInformationRequest.class);
		String custRef = request.getCustReference();

		// fetch customer details from DB (for example)

		// then prepare response
		CustomerInformationResponse.Customer customer = new CustomerInformationResponse.Customer();
		customer.setCustReference(custRef);
		customer.setStatus("0");
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setOtherName("Doe");
		customer.setEmail("example@mail.com");
		customer.setPhone("08012345678");
		customer.setStatusMessage("Successful");
		CustomerInformationResponse response = new CustomerInformationResponse();
		response.setCustomer(List.of(customer));
		return objectToXml(response, CustomerInformationResponse.class);
	}
```

## Payment Notification
When the XML contains 'PaymentNotificationRequest' the implementation should give value to the customer. An example of giving value to the customer could be, crediting the wallet of the customer. After successfully giving value, an appropriate 'Status' (0 for successful and 1 for failed) should be sent in the the response XML. Sample **Payment Notification** payloads are show below, with implementation.

### Sample Payment Notification Request
```
<PaymentNotificationRequest>
    <RouteId>HTTPGENERICv31</RouteId>
    <ServiceUrl>http://yoururlgoeshere.com</ServiceUrl>
    <ServiceUsername />
    <ServicePassword />
    <FtpUrl>http://yoururlgoeshere.com</FtpUrl>
    <FtpUsername />
    <FtpPassword />
    <Payments>
        <Payment>
            <ProductGroupCode>HTTPGENERICv31</ProductGroupCode>
            <PaymentLogId>8963962</PaymentLogId>
            <CustReference>ISP/13/136849/LAG</CustReference>
            <AlternateCustReference>--N/A--</AlternateCustReference>
            <Amount>10000.00</Amount>
            <PaymentStatus>0</PaymentStatus>
            <PaymentMethod>Cash</PaymentMethod>
            <PaymentReference>SBP|BRH|MTBS|7-07-2014|380481</PaymentReference>
            <TerminalId />
            <ChannelName>Bank Branc</ChannelName>
            <Location>ILUPEJU BRANCH</Location>
            <IsReversal>False</IsReversal>
            <PaymentDate>07/07/2014 16:08:34</PaymentDate>
            <SettlementDate>07/08/2014 00:00:01</SettlementDate>
            <InstitutionId>MTBS</InstitutionId>
            <InstitutionName>Mutual Benefits Life Assurance</InstitutionName>
            <BranchName>ILUPEJU BRANCH</BranchName>
            <BankName>Sterling Bank Plc</BankName>
            <FeeName />
            <CustomerName>BRIGHT OSEGHELE EHIZOJIE</CustomerName>
            <OtherCustomerInfo>|</OtherCustomerInfo>
            <ReceiptNo>1418814666</ReceiptNo>
            <CollectionsAccount>900090559901000600</CollectionsAccount>
            <ThirdPartyCode />
            <PaymentItems>
                <PaymentItem>
                    <ItemName>Premium Payment</ItemName>
                    <ItemCode>1100</ItemCode>
                    <ItemAmount>10000.00</ItemAmount>
                    <LeadBankCode>SBP</LeadBankCode>
                    <LeadBankCbnCode>232</LeadBankCbnCode>
                    <LeadBankName>Sterling Bank Plc</LeadBankName>
                    <CategoryCode />
                    <CategoryName />
                    <ItemQuantity>1</ItemQuantity>
                </PaymentItem>
            </PaymentItems>
            <BankCode>SBP</BankCode>
            <CustomerAddress />
            <CustomerPhoneNumber />
            <DepositorName />
            <DepositSlipNumber>6236182</DepositSlipNumber>
            <PaymentCurrency>566</PaymentCurrency>
            <OriginalPaymentLogId />
            <OriginalPaymentReference />
            <Teller>AKINOLA BASHIRU</Teller>
        </Payment>
    </Payments>
</PaymentNotificationRequest>
```

### Sample Payment Notification Response
```
<PaymentNotificationResponse>
    <Payments>
        <Payment>
            <PaymentLogId>8963962</PaymentLogId>
            <Status>0</Status>
            <StatusMessage>Successful</StatusMessage>
        </Payment>
    </Payments>
</PaymentNotificationResponse>
```

### Payment Notification Implementation Snippet
```
	private String notifyOfPayment(String xml) {
		PaymentNotificationRequest request = xmlToObject(xml, PaymentNotificationRequest.class);
		Payment paymentReq = request.getPayment().get(0);
		String custRef = paymentReq.getCustReference();
		String paymentLogId = paymentReq.getPaymentLogId();
		Double amount = Double.parseDouble(paymentReq.getAmount());

		// give value to customer (eg. fund wallet)
		// based on the amount paid by the customer

		// then prepare response
		Payment payment = new Payment();
		payment.setPaymentLogId(paymentLogId);
		payment.setStatus("0");
		payment.setStatusMessage("Successful");
		PaymentNotificationResponse response = new PaymentNotificationResponse();
		response.setPayment(List.of(payment));
		return objectToXml(response, PaymentNotificationResponse.class);
	}
```

The complete implementation of this demo is in 'BillsPaymentApplication.java'.

# Installation
If you are running Java 17 or higher and have maven installed, you can execute the following commands to run the project:
```
git clone https://github.com/scaihai/Interswitch-Bills-Payment-API.git
cd Interswitch-Bills-Payment-API
mvn clean install
mvn spring-boot:run
```

This starts the application on port 9090 and exposes an endpoint with the following URL:
http://localhost:9090/isw-bills-payment
Then make a request using either the customer validation request payload or payment notification request payload.

For more details, please refer to the 'Generic BillPayment Interface API v4.1.pdf' documentation.
