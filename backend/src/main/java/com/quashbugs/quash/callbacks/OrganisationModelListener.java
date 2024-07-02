package com.quashbugs.quash.callbacks;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.service.SequenceGeneratorService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class OrganisationModelListener extends AbstractMongoEventListener<Organisation> {

    private final SequenceGeneratorService sequenceGenerator;

    public OrganisationModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Organisation> entity) {
        if (entity.getSource().getId() < 1) {
            entity.getSource().setId(sequenceGenerator.generateSequence(Organisation.SEQUENCE_NAME));
        }
    }
}
