package org.fenixedu.academictreasury.domain.emoluments;

import java.util.stream.Stream;

import org.fenixedu.academic.domain.serviceRequests.AcademicServiceRequestSituationType;
import org.fenixedu.academic.domain.serviceRequests.ServiceRequestType;
import org.fenixedu.academictreasury.domain.exceptions.AcademicTreasuryDomainException;
import org.fenixedu.academictreasury.domain.serviceRequests.ITreasuryServiceRequest;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.paymentcodes.pool.PaymentCodePool;

import pt.ist.fenixframework.Atomic;

public class ServiceRequestMapEntry extends ServiceRequestMapEntry_Base {

    public ServiceRequestMapEntry() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected ServiceRequestMapEntry(final Product product, final ServiceRequestType requestType,
            final AcademicServiceRequestSituationType createEventOnSituation, final boolean generatePaymentCode,
            final PaymentCodePool paymentCodePool) {
        this();

        setProduct(product);
        setServiceRequestType(requestType);
        setCreateEventOnSituation(createEventOnSituation);
        setGeneratePaymentCode(generatePaymentCode);
        setPaymentCodePool(paymentCodePool);

        checkRules();
    }

    private void checkRules() {

        if (getProduct() == null) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.product.required");
        }

        if (getServiceRequestType() == null) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.serviceRequestType.required");
        }

        if (getCreateEventOnSituation() == null) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.createEventOnSituation.required");
        }

        if (find(getServiceRequestType()).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.duplicatedForServiceRequestType");
        }

    }

    private boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.delete.impossible");
        }

        setServiceRequestType(null);
        setProduct(null);
        setBennu(null);
        super.deleteDomainObject();
    }

    /*---------
     * SERVICES
     * --------
     */

    public static Stream<ServiceRequestMapEntry> findAll() {
        return Bennu.getInstance().getServiceRequestMapEntriesSet().stream();
    }

    public static Stream<ServiceRequestMapEntry> find(final Product product) {
        return findAll().filter(e -> e.getProduct() == product);
    }

    public static Stream<ServiceRequestMapEntry> find(final ServiceRequestType requestType) {
        return findAll().filter(e -> e.getServiceRequestType() == requestType);
    }

    public static Stream<ServiceRequestMapEntry> find(final Product product, final ServiceRequestType requestType) {
        return find(product).filter(e -> e.getServiceRequestType() == requestType);
    }

    public static ServiceRequestMapEntry findMatch(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        final ServiceRequestType serviceRequestType = iTreasuryServiceRequest.getServiceRequestType();
        if (find(serviceRequestType).count() > 1) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.duplicatedForServiceRequestType");
        }
        return find(serviceRequestType).findFirst().orElse(null);
    }

    public static Product findProduct(final ITreasuryServiceRequest iTreasuryServiceRequest) {
        if (findMatch(iTreasuryServiceRequest) == null) {
            throw new AcademicTreasuryDomainException("error.ServiceRequestMapEntry.cannot.find.serviceRequestMapEntry");
        }

        return findMatch(iTreasuryServiceRequest).getProduct();
    }

    @Atomic
    public static ServiceRequestMapEntry create(final Product product, final ServiceRequestType requestType,
            AcademicServiceRequestSituationType situationType) {
        return new ServiceRequestMapEntry(product, requestType, situationType, false, null);
    }

    @Atomic
    public static ServiceRequestMapEntry create(final Product product, final ServiceRequestType requestType,
            AcademicServiceRequestSituationType situationType, final boolean generatePaymentCode,
            final PaymentCodePool paymentCodePool) {
        return new ServiceRequestMapEntry(product, requestType, situationType, generatePaymentCode, paymentCodePool);
    }

}
