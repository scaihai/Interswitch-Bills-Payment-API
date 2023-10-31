package com.interswitch.bills_payment;

import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.Setter;

@SpringBootApplication
@RestController
public class BillsPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillsPaymentApplication.class, args);
	}

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

	// Gets the details of the customer (such as name and address)
	// using the 'custReference' as the customer identifier
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

	// Gives value to the customer for the amount paid
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

	private static <T> String objectToXml(T obj, Class<T> clazz) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			jaxbMarshaller.marshal(obj, stringWriter);
			return stringWriter.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T xmlToObject(String xml, Class<T> clazz) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			return clazz.cast(jaxbContext.createUnmarshaller().unmarshal(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "CustomerInformationRequest")
class CustomerInformationRequest {
	private String MerchantReference;
	private String CustReference;
	private String ServiceUsername;
    private String ServicePassword;
   	private String FtpUsername;
   	private String FtpPassword;
}

@Getter
@Setter
@XmlRootElement(name = "CustomerInformationResponse")
@XmlAccessorType(XmlAccessType.FIELD)
class CustomerInformationResponse {

	private String MerchantReference;
	@XmlElementWrapper(name = "Customers")
	private List<Customer> Customer;

	@XmlAccessorType(XmlAccessType.FIELD)
	@Getter
	@Setter
	static class Customer {

		private String Status;
		private String CustReference = "4565";
		private String FirstName;
		private String LastName;
		private String OtherName;
		private String Email;
		private String Phone;
		private String ThirdPartyCode;
		private String StatusMessage;
	}
}

@Getter
@Setter
@XmlRootElement(name = "PaymentNotificationRequest")
class PaymentNotificationRequest {
	

	private String RouteId;
	private String ServiceUrl;
	private String ServiceUsername;
	private String ServicePassword;
	private String FtpUrl;
	private String FtpUsername;
	private String FtpPassword;
	@XmlElementWrapper(name = "Payments")
	private List<Payment> Payment;
}


@Getter
@Setter
@XmlRootElement(name = "PaymentNotificationResponse")
@XmlAccessorType(XmlAccessType.FIELD)
class PaymentNotificationResponse {

	// <PaymentNotificationResponse>
	// <Payments>
	// <Payment>
	// <PaymentLogId>3193831</PaymentLogId>
	// <Status>0</Status>
	// <StatusMessage>Payment Received</StatusMessage>
	// </Payment>
	// </Payments>
	// </PaymentNotificationResponse>

	@XmlElementWrapper(name = "Payments")
	private List<Payment> Payment;
}

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
class Payment {
	private String ProductGroupCode;
	private String PaymentLogId;
	private String CustReference;
	private String AlternateCustReference;
	private String Amount;
	private String PaymentStatus;
	private String PaymentMethod;
	private String PaymentReference;
	private String TerminalId;
	private String ChannelName;
	private String Location;
	private String IsReversal;
	private String PaymentDate;
	private String SettlementDate;
	private String InstitutionId;
	private String InstitutionName;
	private String BranchName;
	private String BankName;
	private String FeeName;
	private String CustomerName;
	private String OtherCustomerInfo;
	private String ReceiptNo;
	private String CollectionsAccount;
	private String ThirdPartyCode;
	@XmlElementWrapper(name = "PaymentItems")
	private List<PaymentItem> PaymentItem;
	private String BankCode;
	private String CustomerAddress;
	private String CustomerPhoneNumber;
	private String DepositorName;
	private String DepositSlipNumber;
	private String PaymentCurrency;
	private String OriginalPaymentLogId;
	private String OriginalPaymentReference;
	private String Teller;
	private String Status;
	private String StatusMessage;

	@XmlAccessorType(XmlAccessType.FIELD)
	@Getter
	@Setter
	static class PaymentItem {
		private String ItemName;
		private String ItemCode;
		private String ItemAmount;
		private String LeadBankCode;
		private String LeadBankCbnCode;
		private String LeadBankName;
		private String CategoryCode;
		private String CategoryName;
		private String ItemQuantity;
	}
}
