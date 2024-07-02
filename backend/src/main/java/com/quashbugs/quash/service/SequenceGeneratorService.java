package com.quashbugs.quash.service;

import com.quashbugs.quash.model.DatabaseSequence;
import com.quashbugs.quash.model.ReportSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    @Autowired
    private MongoOperations mongoOperations;

    public long generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class
        );
        return counter != null ? counter.getSeq() : 1;
    }

    public int getNextSequenceNumber(String organisationId) {
        Query query = new Query(Criteria.where("_id").is(organisationId));
        Update update = new Update().inc("currentSeqNumber", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);
        ReportSequence seqId = mongoOperations.findAndModify(query, update, options, ReportSequence.class);

        return seqId.getCurrentSeqNumber();
    }
}
