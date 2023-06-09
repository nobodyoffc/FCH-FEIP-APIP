package publish;

import FeipClass.Proof;
import FeipClass.Statement;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.google.gson.Gson;
import fcTools.ParseTools;
import opReturn.Feip;
import opReturn.OpReturn;
import servers.EsTools;
import startFEIP.IndicesFEIP;
import startFEIP.StartFEIP;

import java.io.IOException;
import java.util.ArrayList;

public class PublishParser {

    public ProofHistory makeProof(OpReturn opre, Feip feip) {
        // TODO Auto-generated method stub
        Gson gson = new Gson();
        ProofRaw proofRaw = new ProofRaw();

        try {
            proofRaw = gson.fromJson(gson.toJson(feip.getData()),ProofRaw.class);
        }catch(com.google.gson.JsonSyntaxException e) {
            return null;
        }

        ParseTools.gsonPrint(proofRaw);

        ProofHistory proofHist = new ProofHistory();

        if(proofRaw.getOp()==null)return null;

        proofHist.setOp(proofRaw.getOp());

        switch(proofRaw.getOp()) {
            case "issue":
                if(proofRaw.getTitle()==null) return null;
                if(proofRaw.getContent()==null) return null;
                if (opre.getHeight() > StartFEIP.CddCheckHeight && opre.getCdd() < StartFEIP.CddRequired * 100)
                    return null;
                proofHist.setTxId(opre.getTxId());
                proofHist.setProofId(opre.getTxId());
                proofHist.setHeight(opre.getHeight());
                proofHist.setIndex(opre.getTxIndex());
                proofHist.setTime(opre.getTime());
                proofHist.setSigner(opre.getSigner());
                proofHist.setRecipient(opre.getRecipient());

                proofHist.setTitle(proofRaw.getTitle());
                proofHist.setContent(proofRaw.getContent());

                if(proofRaw.getCosigners()!=null)
                        proofHist.setCosigners(proofRaw.getCosigners());
                proofHist.setTransferable(proofRaw.isTransferable());

                proofHist.setAllSignsRequired(proofRaw.isAllSignsRequired());
                break;
            case "sign":
            case "destroy":
                if(proofRaw.getProofId()==null) return null;
                proofHist.setProofId(proofRaw.getProofId());
                proofHist.setTxId(opre.getTxId());
                proofHist.setHeight(opre.getHeight());
                proofHist.setIndex(opre.getTxIndex());
                proofHist.setTime(opre.getTime());
                proofHist.setSigner(opre.getSigner());
                break;
            case "transfer":
                if(proofRaw.getProofId()==null) return null;
                proofHist.setProofId(proofRaw.getProofId());
                proofHist.setTxId(opre.getTxId());
                proofHist.setHeight(opre.getHeight());
                proofHist.setIndex(opre.getTxIndex());
                proofHist.setTime(opre.getTime());
                proofHist.setSigner(opre.getSigner());
                proofHist.setRecipient(opre.getRecipient());
                break;
            default:
                return null;
        }
        //TODO
        System.out.println("proof made.");
        ParseTools.gsonPrint(proofHist);
        return proofHist;
    }


    public boolean parseStatement(ElasticsearchClient esClient, OpReturn opre, Feip feip) throws ElasticsearchException, IOException {
        // TODO Auto-generated method stub
        boolean isValid = false;

        Gson gson = new Gson();

        StatementRaw statementRaw = new StatementRaw();

        try {
            statementRaw = gson.fromJson(gson.toJson(feip.getData()),StatementRaw.class);
        }catch(com.google.gson.JsonSyntaxException e) {
            return isValid;
        }

        Statement statement = new Statement();

        statement.setStatementId(opre.getTxId());

        if(statementRaw.getConfirm()==null)return isValid;

        if(!statementRaw.getConfirm().equals("This is a formal and irrevocable statement."))return isValid;

        if(statementRaw.getTitle()==null && statementRaw.getContent()==null)return isValid;

        if(statementRaw.getTitle()!=null) {
            statement.setTitle(statementRaw.getTitle());
        }

        if(statementRaw.getContent()!=null) {
            statement.setContent(statementRaw.getContent());
        }

        statement.setOwner(opre.getSigner());
        statement.setBirthTime(opre.getTime());
        statement.setBirthHeight(opre.getHeight());

        esClient.index(i->i.index(IndicesFEIP.StatementIndex).id(statement.getStatementId()).document(statement));
        isValid = true;

        return isValid;
    }


    public boolean parseProof(ElasticsearchClient esClient, ProofHistory proofHist) throws ElasticsearchException, IOException {
        // TODO Auto-generated method stub
        Proof proof;
        switch(proofHist.getOp()) {
            case "issue":
                proof = EsTools.getById(esClient, IndicesFEIP.ProofIndex, proofHist.getProofId(), Proof.class);
                if(proof!=null)return false;

                proof = new Proof();
                proof.setProofId(proofHist.getTxId());
                proof.setTitle(proofHist.getTitle());
                proof.setContent(proofHist.getContent());
                proof.setActive(true);

                if(proofHist.getCosigners()!=null && proofHist.getCosigners().length>0){
                    String[] cosigners = proofHist.getCosigners();
                    ArrayList<String> cosignerList = new ArrayList<>();
                    for(String signer: cosigners){
                        if (signer.equals(proofHist.getSigner()))continue;
                        cosignerList.add(signer);
                    }
                    proof.setCosignersInvited(cosignerList.toArray(new String[0]));
                    if(proofHist.isAllSignsRequired()&&proof.getCosignersInvited().length>0)
                        proof.setActive(false);
                }

                proof.setTransferable(proofHist.isTransferable());

                proof.setIssuer(proofHist.getSigner());

                if(proofHist.getRecipient()!=null) {
                    proof.setOwner(proofHist.getRecipient());
                }else proof.setOwner(proofHist.getSigner());

                proof.setBirthTime(proofHist.getTime());
                proof.setBirthHeight(proofHist.getHeight());

                proof.setLastTxId(proofHist.getTxId());
                proof.setLastTime(proofHist.getTime());
                proof.setLastHeight(proofHist.getHeight());

                //TODO
                System.out.println("proof made.");
                ParseTools.gsonPrint(proof);

                Proof proof1=proof;

                esClient.index(i->i.index(IndicesFEIP.ProofIndex).id(proofHist.getProofId()).document(proof1));
                return true;

            case "sign":

                proof = EsTools.getById(esClient, IndicesFEIP.ProofIndex, proofHist.getProofId(), Proof.class);

                if(proof==null) return false;

                if(proof.isDestroyed()) return false;

                if(proof.getCosignersInvited()==null)return false;

                for(String signer:proof.getCosignersInvited()) {
                    if(proofHist.getSigner().equals(signer)){
                        if(proof.getCosignersSigned()!=null) {
                            for (String signed : proof.getCosignersSigned()) {
                                if (signer.equals(signed)) return false;
                            }
                        }
                        String[] cosignerSigned;
                        if(proof.getCosignersSigned()==null) {
                            cosignerSigned = new String[]{signer};
                        }else {
                            cosignerSigned = new String[proof.getCosignersSigned().length+1];
                            for(int i= 0; i<cosignerSigned.length-1;i++) cosignerSigned[i]=proof.getCosignersSigned()[i];
                            cosignerSigned[cosignerSigned.length-1]=signer;
                        }

                        if(cosignerSigned.length==proof.getCosignersInvited().length)proof.setActive(true);

                        proof.setCosignersSigned(cosignerSigned);
                        proof.setLastTxId(proofHist.getTxId());
                        proof.setLastTime(proofHist.getTime());
                        proof.setLastHeight(proofHist.getHeight());
                        Proof finalProof = proof;
                        esClient.index(i->i.index(IndicesFEIP.ProofIndex).id(proofHist.getProofId()).document(finalProof));
                        return true;
                    }
                }
                return false;

            case "transfer":

                proof = EsTools.getById(esClient, IndicesFEIP.ProofIndex, proofHist.getProofId(), Proof.class);

                if(proof==null) return false;

                if(proof.isDestroyed() || !proof.isActive()) return false;

                if(! proof.getOwner().equals(proofHist.getSigner())) return false;

                if(proofHist.getRecipient()==null) return false;

                proof.setOwner(proofHist.getRecipient());

                Proof finalProof1 = proof;
                esClient.index(i->i.index(IndicesFEIP.ProofIndex).id(proofHist.getProofId()).document(finalProof1));
                return  true;

            case "destroy":

                proof = EsTools.getById(esClient, IndicesFEIP.ProofIndex, proofHist.getProofId(), Proof.class);

                if(proof==null) return false;

                if(proof.isDestroyed() || !proof.isActive()) return false;

                if(! proof.getOwner().equals(proofHist.getSigner())) return false;

                proof.setDestroyed(true);
                proof.setActive(false);

                Proof finalProof2 = proof;
                esClient.index(i->i.index(IndicesFEIP.ProofIndex).id(proofHist.getProofId()).document(finalProof2));
                return  true;
        }
        return false;
    }
}
