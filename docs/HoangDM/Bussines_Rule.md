
### **5.1 Business Rules**

#### ***5.1.1. User & Account Management***

**Authorization & Risk Control**

*This section governs account lifecycle, role-based access, security, and platform risk control.*

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| USR-01 | Shop Approval: A newly registered Shop Owner account must be created with the default status Pending. The account may only be activated as Active after an Admin has approved valid business registration documents or citizen identification documents and the required food safety certificate. | Constraint | Static | Platform Policy / Law |
| USR-02 | Delivery Address Data Protection: A Customer account may store a maximum of 5 delivery addresses. Exactly 1 address in the address list must be marked as Default. | Constraint | Static | System Architect |
| USR-03 | Geolocation Validation: Every new address added through Manage Addresses must be validated through Google Maps API or an equivalent map service to obtain accurate latitude and longitude for delivery zone validation and express delivery calculation. | Constraint | Dynamic | Logistics Standard |
| USR-04 | Order and Review Manipulation Prevention: A Shop Owner is not allowed to place orders for products owned by the same shop. The system must also check related risk signals such as Device ID, IP address, phone number, delivery address, and payment account to prevent fake orders and fake reviews. | Constraint | Dynamic | Risk Management |
| USR-05 | Guest User Limitation: A non-logged-in Guest user is allowed to browse products, view product details, and add products to the shopping cart. However, the user must log in or register before proceeding to checkout. | Action enabler | Static | Security Policy |
| SEC-01 | COD Abuse Prevention: If a Customer has more than 3 failed deliveries caused by buyer refusal or buyer fault within 30 days, the system must automatically disable COD payment for that account permanently until an Admin manually restores the permission. | Constraint | Dynamic | Risk Management |
| SEC-02 | Shop Violation Handling: When an Admin blocks a Shop Owner account, all products owned by that shop must be automatically changed to Hidden, and all pending orders belonging to that shop must be automatically cancelled and refunded where applicable. | Action enabler | Dynamic | Security Policy |

#### ***5.1.2. Product & Inventory***

**Product Catalog & Stock Control**

*This section covers fresh fruit characteristics, traceability, inventory accuracy, and error prevention.*

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| PRD-01 | Sellable Product Variants: Every fruit product must have at least 1 weight-based variant, such as 500g box, 1kg bag, or 3kg carton. The system must not allow selling only by “number of fruits” unless a reference weight is provided. | Constraint | Static | Product Manager |
| PRD-02 | Traceability Requirement: If a product is tagged as Imported Fruit or Organic, the Shop must provide a batch number and upload relevant supporting certificates before the product can be approved for sale. | Constraint | Dynamic | Food Safety Law |
| PRD-03 | Seasonal Availability Automation: Products configured with Seasonal Availability must automatically become Out of Season or have the buy button disabled when the current system date falls outside the configured seasonal period. | Action enabler | Dynamic | Product Logic |
| PRD-04 | Pricing Error Protection: To prevent severe pricing mistakes, the system must block promotional prices that are more than 80% lower than the base price unless manually approved by an Admin. | Constraint | Static | Pricing Policy |
| INV-01 | Real-time Stock Reservation: Stock Quantity must be reserved immediately when the customer proceeds to payment during checkout. The reservation is valid for 15 minutes. If payment is not completed within 15 minutes, the system must automatically release the reserved stock. | Action enabler | Dynamic | Inventory Manager |
| INV-02 | Low Stock Alert: The Low Stock Alert event must automatically notify the Shop Owner when the total stock quantity of a variant falls below the minimum threshold, for example below 5kg or below the shop-configured threshold. | Fact | Dynamic | Inventory Manager |
| INV-03 | Hide Out-of-stock Products: If all weight variants of a product have Stock Quantity \= 0, the product must not be displayed in Recommendation or Best Sellers sections. | Action enabler | Dynamic | UX / Marketing |

#### ***5.1.3. Order & Instant Delivery***

**Order Handling & Delivery Operations**

*The core business concern is express delivery while reducing bruising, spoilage, and cancellation risks.*

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| DEL-01 | Delivery Radius Limitation: Checkout is only allowed if the customer’s delivery address is within a maximum radius of 20km from the Shop Owner’s warehouse or pickup location. | Constraint | Dynamic | Logistics Policy |
| DEL-02 | Express Delivery Load Limit: The total weight of a cart using express delivery must not exceed 30kg to ensure it can be transported by a motorbike or standard delivery vehicle. | Constraint | Dynamic | Logistics API |
| DEL-03 | Estimated Delivery Time Calculation: Estimated Delivery Time \= Shop Default Preparation Time \+ Estimated Travel Time from Map API. Example: 30 minutes of preparation \+ 25 minutes of travel \= 55 minutes ETA. | Computation | Dynamic | Logistics API |
| ORD-01 | Order Cancellation Condition: A customer may only cancel an order while it is in Pending status. Once the Shop updates the order to Processing or has started cutting, weighing, or packing the fruit, the customer cannot cancel the order by themselves. | Constraint | Dynamic | Shop Policy |

#### ***5.1.4. Payment, Promotions*** 

**Payment, Promotion & Accounting Rules**

*This section supports legal compliance, revenue protection, and coupon fraud prevention.*

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| PRO-01 | Coupon Stacking Rule: A customer must not use 2 order-level Discount Coupons at the same time. However, the customer may use 1 Discount Coupon together with 1 Free-shipping Coupon for the same invoice if the campaign configuration allows it. | Constraint | Static | Marketing Policy |
| PRO-02 | Flash Sale Timeout: If a product in the cart belongs to a Flash Sale program, the customer must complete payment before the Flash Sale countdown ends. If the countdown expires, the product price must automatically revert to Base Pricing. | Computation | Dynamic | Marketing Policy |
| PRO-03 | Promotion Budget Control: Every Discount Coupon must have either a maximum budget limit or a maximum usage limit. Once the configured threshold is reached, the coupon must automatically change to Expired. | Action enabler | Dynamic | Financial Policy |
| PAY-01 | COD Limit: Cash on Delivery is only available for orders with Invoice Total below 2,000,000 VND. Orders above this threshold must be prepaid by card or e-wallet. | Constraint | Static | Financial Policy |
| PAY-02 | Online Payment Timeout: For Credit Card, Debit Card, or E-wallet payments, the payment session must be completed within 10 minutes. If the session expires, the transaction must be cancelled and the reserved stock must be released. | Fact | Dynamic | Payment Gateway API |
| PAY-03 | Shop Settlement Holding Period: Sales proceeds must be held in the Shop’s system wallet and may only be moved to Available Balance after 12 hours from successful delivery, provided that no complaint or dispute has been raised. | Constraint | Static | Platform Finance |
| ORD-02 | Invoice Total Calculation: Invoice Total \= Σ\[(Base Price \- Discount Price) × Qty\] \+ Shipping Fee \- Promotional Coupon. The final Invoice Total must be at least 0 VND and must never become negative. | Computation | Dynamic | Accounting Code |

#### ***5.1.5. Review & Refund System***

**Product Review & Complaint Handling**

*This section handles fresh produce complaints, spoilage, bruising, shortage, and quality disputes.*

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| REF-01 | Complaint Window: For fresh fruit products, a Customer may only create a Refund Request within 6 hours from the delivery completion time. After 6 hours, the complaint button must be disabled. | Constraint | Dynamic | Refund Policy |
| REF-02 | Refund Evidence Requirement: Every Refund Request must include at least 1 video or 2 clear photos showing damaged, bruised, rotten, missing, or incorrect products. The default policy is partial/full refund without physical return to reduce logistics cost. | Constraint | Static | Customer Service |
| REF-03 | Electronic Refund: If a complaint is approved by an Admin, the refund must be returned to the original online payment method where applicable, or to the platform wallet for COD orders. | Action enabler | Dynamic | Finance Policy |
| REV-01 | Review Eligibility: The Write Review feature is only unlocked for products in successfully delivered orders. A user cannot review a product that they have not purchased or that has not been delivered. | Constraint | Dynamic | System Logic |
| REV-02 | Rating Formula: Average Rating \= Total Valid Stars / Total Valid Reviews. Reviews rejected by Admin, spam reviews, or fraudulent reviews must be excluded from the average rating calculation. | Computation | Dynamic | System Logic |

#### ***5.1.6. Shop Onboarding, Compliance & Seller Risk***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| SHP-01 | A Shop Owner may only move from PendingVerification to Active after all mandatory documents have been successfully verified, including identity information, warehouse address, payout information, and business or food safety documents required by platform policy. | Constraint | Dynamic | Platform Compliance |
| SHP-02 | A Shop with expired legal documents or food safety certificates must automatically move to ComplianceHold. While in this status, the Shop cannot create new products, join campaigns, enable flash sales, or withdraw funds. | Action enabler | Dynamic | Compliance Policy |
| SHP-03 | If a Shop’s cancellation rate caused by seller fault exceeds 10% within the last 7 days, the Shop must be temporarily restricted from receiving express delivery orders until its operational rate returns to the safe threshold. | Constraint | Dynamic | Seller Risk Management |
| SHP-04 | If a Shop’s approved quality complaint rate exceeds 5% of successfully delivered orders within 30 days, the system must reduce product visibility and place the Shop into the quality inspection queue. | Action enabler | Dynamic | Quality Risk Policy |
| SHP-05 | A Shop may operate multiple warehouses or pickup points, but each product or variant must be mapped to at least one specific warehouse to support stock control, delivery zone validation, and ETA calculation. | Constraint | Static | Marketplace Architecture |
| SHP-06 | A Shop may only enable COD if its profile is verified, it is not under ComplianceHold, it is not risk-restricted, and its successful delivery rate meets the platform-configured threshold. | Action enabler | Dynamic | Payment Risk Policy |

#### ***5.1.7. Freshness, Batch, Shelf-life & Quality Grade***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| FRH-01 | Every inbound fruit batch must have at least the following information: batch code, inbound date, packing date or harvest date where available, recommended sell-by date, inbound quantity, available quantity, and quality status. | Constraint | Static | Fresh Goods Data Model |
| FRH-02 | The system must fulfill orders using FEFO, meaning the batch with the earliest sell-by date or expiration date must be prioritized first unless that batch is locked due to a quality issue. | Action enabler | Dynamic | Fresh Inventory Policy |
| FRH-03 | A batch that has passed its recommended sell-by date, has been marked as damaged, or has failed quality inspection must not be included in availableStock and must not be used to fulfill orders. | Constraint | Dynamic | Food Safety Policy |
| FRH-04 | Grade B or near-expiry products must be clearly displayed as such on the product page, shopping cart, and receipt/invoice. The system must not allow misleading images or descriptions that imply the product is Grade A. | Constraint | Static | Customer Protection Policy |
| FRH-05 | If the estimated delivery time exceeds the quality-safe threshold for a fruit category, the system must disable that delivery option and recommend a closer shop, another delivery time slot, or a more suitable delivery method. | Action enabler | Dynamic | Freshness SLA Policy |
| FRH-06 | Fresh goods that have left the warehouse but failed delivery must not be automatically returned to sellable stock if the safe time limit is exceeded, the package is opened, the package is damaged, or storage conditions are no longer controlled. | Constraint | Dynamic | Food Safety Policy |
| FRH-07 | If a product is sold by actual weight, the allowed weight variance is ±3% from the ordered weight. If the delivered weight is more than 3% below the ordered weight, the system must calculate a refund for the shortage. | Computation | Dynamic | Fresh Produce Policy |

#### ***5.1.8. Cart, Checkout & Multi-shop Order***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| CHK-01 | Products in the shopping cart are only draft selections. Before checkout, the system must revalidate price, stock, Shop status, product status, seasonal availability, coupons, and delivery zone. | Constraint | Dynamic | Checkout Architecture |
| CHK-02 | If the cart contains products from multiple Shops, the system must create 1 Parent Order and multiple Child Orders, grouped by Shop. Each Child Order must have its own shipping fee, processing status, delivery status, refund status, and invoice. | Action enabler | Dynamic | Marketplace Architecture |
| CHK-03 | Cancellation of one Child Order must not automatically cancel the entire Parent Order. The remaining Child Orders must continue processing if payment, stock, delivery, and legal conditions remain valid. | Constraint | Dynamic | Order Policy |
| CHK-04 | A platform-wide voucher applied at Parent Order level must be allocated down to each Child Order based on the proportion of eligible item value, so refund, reporting, and reconciliation can be performed accurately. | Computation | Dynamic | Promotion Accounting |
| CHK-05 | When a Child Order is cancelled after a platform-wide voucher has been applied, the system must recalculate the allocated voucher amount and ensure the total refund never exceeds the amount paid by the customer. | Computation | Dynamic | Refund Accounting |
| CHK-06 | If available stock is lower than the quantity requested by the customer at order confirmation time, the system must not create the order and must require the customer to update the quantity or remove the product from the cart. | Constraint | Dynamic | Inventory Policy |

#### ***5.1.9. Order Lifecycle, Substitution & Exception Handling***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| OLC-01 | The standard Order lifecycle is Draft → PendingPayment → Paid/CODConfirmed → PendingShopAccept → Preparing → ReadyForPickup → InTransit → Delivered → Completed. The system must not allow a status jump unless a valid event and transition exist. | Constraint | Dynamic | Order Lifecycle |
| OLC-02 | An online prepaid order may only move to PendingShopAccept after the payment gateway confirms successful payment. A COD order skips Paid but must have the status CODConfirmed. | Constraint | Dynamic | Payment Integration |
| OLC-03 | If the Shop does not accept an express order within 5 minutes, the system must send the first reminder. If the Shop still does not respond after 10 minutes, the system must automatically cancel the Child Order, release stock, and refund the corresponding amount if already paid. | Action enabler | Dynamic | Express Delivery SLA |
| OLC-04 | From Preparing onward, the Customer cannot independently modify products, quantity, address, payment method, or coupon. Any change after this point must go through cancellation, adjustment, or Admin approval. | Constraint | Dynamic | Order Control |
| OLC-05 | If the Shop detects stock shortage after the customer has paid, the Shop may only choose one of three handling options: deliver shortage with partial refund, propose a substitute product, or cancel the affected item/Child Order. | Constraint | Dynamic | Fresh Goods Policy |
| OLC-06 | If the Shop proposes a substitute product, the customer must confirm within the configured time limit. If the customer does not respond within that time, the system must automatically cancel the missing item and refund the corresponding amount. | Action enabler | Dynamic | Substitution Policy |
| OLC-07 | If the substitute product is more expensive than the original product, the system may only collect the additional amount after explicit customer confirmation. If the substitute product is cheaper, the system must refund the difference to the customer. | Computation | Dynamic | Substitution Accounting |
| OLC-08 | An order may only move to Completed after it has been delivered and the complaint window has expired, or after all related complaints, refunds, and disputes have been fully resolved. | Constraint | Dynamic | Settlement Policy |
| OLC-09 | If payment succeeds but order creation fails, the system must create a Payment Orphan record and automatically retry order creation or process a refund through the reconciliation workflow. | Action enabler | Dynamic | Exception Handling |
| OLC-10 | If an order has been created but stock reservation fails, the order must move to InventoryException; the system must not collect payment, or must refund the payment if already collected. | Action enabler | Dynamic | Inventory Exception Policy |

#### ***5.1.10. Payment, Wallet, COD Reconciliation & Settlement***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| FIN-01 | Every payment request must include an idempotencyKey. If the payment gateway sends duplicate webhooks, the system must record the payment only once and ignore duplicate events. | Constraint | Dynamic | Payment Architecture |
| FIN-02 | If the customer is charged but the system has not received a successful webhook, the order must move to PaymentVerifying for a maximum of 30 minutes. After that period, the system must automatically reconcile with the gateway before cancelling or confirming the payment. | Action enabler | Dynamic | Payment Reconciliation |
| FIN-03 | The platform must not settle funds to the Shop if the Child Order has not been Delivered, has not been Completed, still has an open refund/dispute, or is in an exception status. | Constraint | Dynamic | Platform Finance |
| FIN-04 | Shop sales proceeds move into PendingBalance after delivery and only move to AvailableBalance when the order is Completed, platform fees and related operational fees have been deducted, and no dispute remains open. | Computation | Dynamic | Settlement Policy |
| FIN-05 | For COD orders, the amount reported as collected by the shipper must match the amount required by the system. If there is a mismatch, the order must move to CODReconciliationException and must not be settled to the Shop. | Constraint | Dynamic | Finance Control |
| FIN-06 | Online refunds must prioritize the original payment method. If the original method does not support refunds or the transaction is too old, the refund must be issued to the platform wallet or to a verified bank account. | Action enabler | Dynamic | Refund Finance Policy |
| FIN-07 | COD refunds must not be paid in cash through the shipper. The system may only refund to the platform wallet or to the Customer’s verified bank account. | Constraint | Dynamic | Refund Finance Policy |
| FIN-08 | A Shop may only create a withdrawal request when AvailableBalance \> 0, the Shop profile is valid, the Shop is not under Compliance Hold, and there is no fund hold caused by a serious refund or dispute. | Action enabler | Dynamic | Wallet Policy |
| FIN-09 | Platform fees must be recorded separately from the Shop’s product revenue. Reports must not merge platform fees into the Shop’s fruit sales revenue. | Constraint | Static | Marketplace Accounting |

#### ***5.1.12. Promotion, Coupon Abuse & Fraud Prevention***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| COU-01 | The minimum order value condition for a coupon must be calculated based on product value after Shop discount but before shipping fee, tax, wallet credit, and platform coupon deduction. | Computation | Dynamic | Promotion Engine |
| COU-02 | A coupon must have at least one control limit, including validity period, total budget, total usage count, usage count per user, or applicable scope by product, Shop, or category. | Constraint | Static | Promotion Governance |
| COU-03 | Coupon budget deduction must be processed atomically. If two customers attempt to use the last available coupon usage at the same time, only the first transaction succeeds and the other transaction must be repriced. | Constraint | Dynamic | System Architect |
| COU-04 | A new-user coupon must not be applied if the system detects duplicated device, phone number, payment account, delivery address, or mass account creation behavior. | Constraint | Dynamic | Fraud Prevention |
| COU-05 | Coupons must not be convertible to cash. If an order is cancelled due to customer fault, the used coupon must not be automatically returned unless the campaign configuration explicitly allows coupon restoration. | Constraint | Dynamic | Marketing Policy |
| COU-06 | A Flash Sale must not sell more than the quantity allocated to the campaign. CampaignStock must be controlled separately or protected by a dedicated quota from normal stock. | Constraint | Dynamic | Promotion Inventory |
| COU-07 | Shop-funded vouchers must be deducted from the issuing Shop’s revenue. Platform-funded vouchers must be recorded as platform marketing cost. The system must not allocate voucher funding to the wrong party. | Computation | Dynamic | Finance Policy |
| COU-08 | If one device or a cluster of identity signals creates multiple accounts using new-user coupons within a short period, related orders must move to Fraud Review. | Action enabler | Dynamic | Fraud Risk Engine |

#### ***5.1.13. Delivery SLA, Proof of Delivery & Logistics Exception***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| DLV-01 | Checkout is only allowed if the delivery address is within the service zone of the Shop or delivery partner, based on warehouse coordinates, customer coordinates, radius, travel time, and product type. | Action enabler | Dynamic | Logistics Policy |
| DLV-02 | ETA must be calculated as Shop Preparation Time \+ Carrier Pickup Time \+ Travel Time \+ Buffer Time. Buffer Time varies by peak hour, weather, distance, and historical delay on the route. | Computation | Dynamic | Logistics Engine |
| DLV-03 | The delivery partner must accept the delivery trip within the configured time. If the time expires, the system must automatically reassign a shipper, but reassignment must not exceed 3 times if it causes ETA to exceed the quality threshold. | Action enabler | Dynamic | Logistics SLA |
| DLV-04 | Valid delivery statuses are AwaitingPickup → PickedUp → InTransit → Arrived → Delivered or FailedDelivery. The system must not allow a shipment to become Delivered without proof of delivery. | Constraint | Dynamic | Delivery Lifecycle |
| DLV-05 | Minimum proof of delivery must include delivery time, delivery coordinates, and either photo evidence or OTP. For COD orders, the collected amount and shipper confirmation are mandatory. | Constraint | Dynamic | Delivery Confirmation |
| DLV-06 | If the customer does not answer the phone or is not available, the shipper must make at least 2 contact attempts, at least 5 minutes apart, before marking the delivery as FailedDelivery. | Constraint | Dynamic | Logistics Policy |
| DLV-07 | If severe weather, road restrictions, or delivery partner suspension occurs, the system must disable express checkout for the affected area and clearly inform the customer of the reason. | Action enabler | Dynamic | Logistics Operations |
| DLV-08 | If delivery is delayed beyond SLA due to Shop fault, compensation or apology coupon cost must be charged to the Shop. If the delay is caused by the carrier, the cost must be charged to the carrier or an operational fund according to configuration. | Computation | Dynamic | SLA Compensation Policy |
| DLV-09 | A Shop must not be able to mark an order as Delivered. The Delivered status may only be updated by the shipper, logistics system, or a privileged Admin with supporting evidence. | Constraint | Dynamic | Fraud Prevention |

#### ***5.1.14. Refund, Dispute & Abuse Control***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| RFD-01 | Each line item may have only one open refund case. A Customer cannot create multiple concurrent refund requests for the same item to prevent double refund. | Constraint | Dynamic | Refund Control |
| RFD-02 | The refund amount for weight shortage must be calculated as Unit Price After Discount × Missing Weight. If the shortage is caused by Shop fault, the platform-funded voucher portion must not be reclaimed from the customer. | Computation | Dynamic | Refund Engine |
| RFD-03 | The refund amount for damaged goods must be calculated as Line Item Paid Amount × Approved Damage Ratio. The damage ratio is determined by Admin or by automated rules based on the submitted evidence. | Computation | Dynamic | Refund Engine |
| RFD-04 | For perishable fresh goods, physical return is not required by default. If the Shop requests return pickup, the request must be approved by Admin and must not extend the refund processing deadline. | Constraint | Dynamic | Fresh Goods Policy |
| RFD-05 | The Shop must respond to a refund case within 12 hours. If the Shop does not respond, the case must move to Admin Review and may be auto-approved if the evidence is clear and the value is below the configured threshold. | Action enabler | Dynamic | Customer Protection |
| RFD-06 | If a refund is approved, the system must update revenue, platform fee, tax, reward points, rating eligibility, and Shop settlement according to the refunded portion. | Action enabler | Dynamic | Finance / Accounting |
| RFD-07 | If a Customer’s approved refund rate is abnormally high compared with similar behavior groups, future refund requests must go through Admin Review and must not be auto-approved. | Constraint | Dynamic | Refund Abuse Prevention |
| RFD-08 | If a refund has been approved but refund payment fails, the refund case must move to RefundPaymentFailed; the system must retry according to configuration or move the case to the Finance manual handling queue. | Action enabler | Dynamic | Refund Exception Handling |

## 

#### ***5.1.15. Review, Rating & Content Moderation***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| RVR-01 | A review may only be edited within 7 days after creation and must not be edited after Admin has processed a serious content violation. | Constraint | Dynamic | Review Moderation |
| RVR-02 | Rejected reviews, spam reviews, fraudulent reviews, offensive reviews, or reviews unrelated to the product must not be included in Average Rating. | Constraint | Dynamic | Review Policy |
| RVR-03 | If the system detects that a Shop is using related accounts to create fake reviews, the reviews must be hidden, the Shop trust score must be reduced, and the Shop must be placed into the fraud inspection queue. | Action enabler | Dynamic | Fraud Prevention |
| RVR-04 | A Customer must not review a product if the order was cancelled before delivery, fully refunded due to non-delivery, or the product was not part of a Delivered order. | Constraint | Dynamic | Review Eligibility |
| RVR-05 | The rating displayed on the product listing must be updated through near-real-time processing or scheduled batch jobs. The maximum update delay must be clearly configured to avoid report inconsistency. | Fact | Dynamic | Rating Engine |

#### ***5.1.16. Notification, Admin Control, Reporting & Data Integrity***

| ID | Rule definition | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| NTF-01 | Critical events such as order creation, successful payment, Shop acceptance, delivery in progress, successful delivery, and approved refund must generate notifications using an idempotent mechanism to prevent duplicate notifications. | Constraint | Dynamic | Notification Architecture |
| NTF-02 | If an express delivery is delayed beyond SLA, notifications must be sent to the Customer, Shop, and Operations team. The notification must include the new ETA and reason where available. | Action enabler | Dynamic | Logistics SLA |
| NTF-03 | New Order Alert must be sent to the Shop in real time. If the Shop is offline, the system may additionally send push or email notifications according to configuration, but this must not extend the order acceptance SLA. | Action enabler | Dynamic | Shop Operations |
| ADM-01 | Admin users may only approve Shops, products, refunds, or account blocks according to assigned permissions. A lower-level Admin must not approve transactions created by themselves. | Constraint | Static | Internal Control |
| ADM-02 | An Admin-managed Product Category must not be deleted if it still contains active products. The system may only allow the category to be marked inactive or merged into another category. | Constraint | Dynamic | Catalog Governance |
| ADM-03 | Revenue reports must be separated at minimum into GMV, Shop revenue, platform fees, shipping fees, platform-funded vouchers, Shop-funded vouchers, refunds, tax, and net revenue. | Constraint | Static | Reporting Policy |
| ADM-04 | Sales Report must not count orders with Cancelled, FailedPayment, or FraudRejected status as revenue. Refunded orders must only record net revenue after refund. | Computation | Dynamic | Accounting Report |
| ADM-05 | Dashboard Order Statistics must count by Child Order for Shop operations and by Parent Order for customer purchasing behavior. The two counting methods must not be mixed in the same KPI. | Constraint | Static | Analytics Policy |
| DAT-01 | All monetary values must be stored in the smallest currency unit, for example VND as integer. Floating-point storage must not be used for money to avoid rounding errors. | Constraint | Static | Data Model |
| DAT-02 | All core objects including User, Shop, Product, Variant, Batch, Cart, Order, Payment, Invoice, Shipment, and Refund must have immutable IDs and must not reuse IDs after soft deletion. | Constraint | Static | Data Model |
| DAT-03 | The statuses of Order, Payment, Shipment, and Refund must be implemented as state machines. APIs must not allow the client to directly submit a target status if no valid transition exists. | Constraint | Dynamic | System Architect |
| DAT-04 | Every webhook from payment, logistics, and e-invoice systems must store the raw payload, signature verification result, received timestamp, and processing result for reconciliation and incident investigation. | Constraint | Dynamic | Integration Architecture |
| DAT-05 | If the system detects inconsistency between payment, order, and shipment data, the order must move to an exception status. The system must not settle, auto-complete, or recognize net revenue until the exception is resolved. | Action enabler | Dynamic | Data Integrity Policy |
| DAT-06 | After an order has been completed for more than 90 days, the Shop interface may only display masked customer address and phone number. An Admin who needs full access must have special permission and enter an access reason. | Constraint | Dynamic | Personal Data Protection |
| DAT-07 | All high-risk Admin actions, such as blocking a Shop, approving a high-value refund, adjusting wallet balance, modifying tax rate, or removing a best-selling product, must be recorded in audit logs with actor, timestamp, old value, new value, and reason. | Constraint | Dynamic | Internal Audit Policy |

## 

| Priority | Rule group | Reason |
| :---- | :---- | :---- |
| P0 | Order, Payment, Inventory Hold, Refund, COD | Directly impacts money, stock, customer experience, and revenue leakage risk. |
| P0 | State Machines for Order/Payment/Shipment/Refund | Prevents Developers and QA from interpreting status flows differently. |
| P0 | E-Invoice, refund after invoice, payment reconciliation | High risk of legal, tax, and accounting errors. |
| P1 | Batch, Freshness, FEFO, Quality Grade | Core domain characteristics of fresh fruit products. |
| P1 | Multi-shop Order and voucher allocation | Required for a Shopee-like marketplace model. |
| P1 | Audit Log and Data Masking | Required for security, traceability, and personal data protection. |
| P2 | Fraud Score, Coupon Abuse, Review Fraud | Improves platform safety as transaction volume grows. |

## 

| Type of rule | Meaning |
| :---- | :---- |
| Fact | Describes a business fact or system event that must be recorded or recognized. |
| Constraint | Defines a restriction, limitation, or blocking condition when business conditions are not satisfied. |
| Computation | Defines calculations for money, fees, tax, rating, stock, ETA, or reporting metrics. |
| Action enabler | Enables, triggers, or automatically performs an action when the required conditions are satisfied. |