package org.apache.stanbol.entityhub.ldpath;

import org.apache.stanbol.entityhub.core.mapping.ValueConverterFactory.AnyUriConverter;
import org.apache.stanbol.entityhub.core.mapping.ValueConverterFactory.ReferenceConverter;
import org.apache.stanbol.entityhub.core.mapping.ValueConverterFactory.TextConverter;
import org.apache.stanbol.entityhub.core.mapping.ValueConverterFactory.ValueConverter;
import org.apache.stanbol.entityhub.core.model.InMemoryValueFactory;
import org.apache.stanbol.entityhub.ldpath.transformer.ValueConverterTransformerAdapter;
import org.apache.stanbol.entityhub.servicesapi.defaults.DataTypeEnum;
import org.apache.stanbol.entityhub.servicesapi.model.Reference;
import org.apache.stanbol.entityhub.servicesapi.model.Representation;
import org.apache.stanbol.entityhub.servicesapi.model.Text;
import org.apache.stanbol.entityhub.servicesapi.model.ValueFactory;

import at.newmedialab.ldpath.LDPath;
import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.transformers.NodeTransformer;
import at.newmedialab.ldpath.model.fields.FieldMapping;
import at.newmedialab.ldpath.model.programs.Program;

/**
 * {@link LDPath} with Entityhub specific configurations.
 * In detail this registers {@link NodeTransformer} for:<ul>
 * <li> {@link DataTypeEnum#Reference} returning {@link Reference} instances
 * <li> xsd:anyURI also returning {@link Reference} instances
 * <li> {@link DataTypeEnum#Text} returning {@link Text} instances
 * <li> xsd:string also returning {@link Text} instances
 * </ul><p>
 * It adds also support for returning {@link Representation} instances as
 * result of executing {@link Program}s on a context. This is important because
 * it allows a seamless integration of LDPath with existing Entityhub
 * functionality/interfaces
 * 
 * <p>Because there is currently
 * no way to get the LDPath parser to instantiate an extension of {@link Program}
 * this feature is currently implemented by {@link #execute(Reference, Program)}
 * of this class.
 * 
 * @author Rupert Westenthaler
 *
 */
public class EntityhubLDPath extends LDPath<Object> {

    private final ValueFactory vf;
    private final RDFBackend<Object> backend;
    public EntityhubLDPath(RDFBackend<Object> backend) {
        this(backend,null);
    }
    public EntityhubLDPath(RDFBackend<Object> backend,ValueFactory vf) {
        super(backend);
        this.vf = vf == null ? InMemoryValueFactory.getInstance() : vf;
        this.backend = backend;
        //register special Entutyhub Transformer for
        // * entityhub:reference
        ValueConverter<Reference> referenceConverter = new ReferenceConverter();
        registerTransformer(referenceConverter.getDataType(), 
            new ValueConverterTransformerAdapter<Reference>(referenceConverter,vf));
        // * xsd:anyURI
        ValueConverter<Reference> uriConverter = new AnyUriConverter();
        registerTransformer(uriConverter.getDataType(), 
            new ValueConverterTransformerAdapter<Reference>(uriConverter,vf));
        // * entityhub:text
        ValueConverter<Text> literalConverter = new TextConverter();
        registerTransformer(literalConverter.getDataType(), 
            new ValueConverterTransformerAdapter<Text>(literalConverter,vf));
        // xsd:string (use also the literal converter for xsd:string
        registerTransformer(DataTypeEnum.String.getUri(), 
            new ValueConverterTransformerAdapter<Text>(literalConverter,vf));
        //TODO: Currently it is NOT possible to register the default
        //      Namespace mappings defined for the Entityhub!
    }
    
    /**
     * Executes the parsed {@link Program} and stores the 
     * {@link Program#getFields() fields} in a {@link Representation}. The actual
     * implementation used for the {@link Representation} depends on the
     * {@link ValueFactory} of this EntityhubLDPath instance
     * @param context the context
     * @param program the program 
     * @return the {@link Representation} holding the results of the execution
     * @throws IllegalArgumentException if the parsed context or the program is
     * <code>null</code>
     */
    public Representation execute(Reference context,Program<Object> program){
        if(context == null){
            throw new IllegalArgumentException("The parsed context MUST NOT be NULL!");
        }
        if(program == null){
            throw new IllegalArgumentException("The parsed program MUST NOT be NULL!");
        }
        Representation result = vf.createRepresentation(context.getReference());
        for(FieldMapping<?,Object> mapping : program.getFields()) {
            result.add(mapping.getFieldName(),mapping.getValues(backend,context));
        }
        return result;
        
    }
}