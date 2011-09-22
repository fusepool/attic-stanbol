/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.stanbol.commons.opennlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextAnalyzer {
    
    private static final Logger log = LoggerFactory.getLogger(TextAnalyzer.class);
    @Reference
    private final OpenNLP openNLP;
    /**
     * The language of the analysed text
     */
    //protected final String language; //protected to speed up access by internal classes
    private boolean forceSimpleTokenizer = false; //default to false
    private boolean enablePosTagger = true;
    private boolean enableChunker = true;
    private boolean enableSentenceDetector = true;
    private boolean enablePosTypeChunker = true;
    private boolean forcePosTypeChunker = true;
    
    //private POSTaggerME posTagger;
    //private SentenceDetector sentenceDetector;
    //private ChunkerME chunker;
    //private PosTypeChunker posTypeChunker;
    //private Tokenizer tokenizer;
    /**
     * PosTypeChunkers for the different languages
     */
    private Map<String,PosTypeChunker> posTypeChunkers = new HashMap<String,PosTypeChunker>();

    
    public TextAnalyzer(OpenNLP openNLP){
        if(openNLP == null){
            throw new IllegalArgumentException("The OpenNLP component MUST NOT be NULL");
        }
        this.openNLP = openNLP;
    }

    protected final POSTaggerME getPosTagger(String language) {
        if(!enablePosTagger){
            return null;
        }
        POSTaggerME posTagger;
        try {
            POSModel posModel = openNLP.getPartOfSpeachModel(language);
            if(posModel != null){
                posTagger = new POSTaggerME(posModel);
            } else {
                log.debug("No POS Model for language {}",language);
                posTagger = null;
            }
        } catch (IOException e) {
            log.info("Unable to load POS Model for language "+language,e);
            posTagger = null;
        }
        return posTagger;
    }
    /**
     * Getter for the Tokenizer of a given language
     * @param language the language
     * @return the Tolenizer
     */
    public final Tokenizer getTokenizer(String language){
        Tokenizer tokenizer;
        if(forceSimpleTokenizer){
            tokenizer = SimpleTokenizer.INSTANCE;
        } else {
            tokenizer = openNLP.getTokenizer(language);
        }
        return tokenizer;
    }
    protected final ChunkerME getChunker(String language){
        if(!enableChunker || forcePosTypeChunker){
            return null;
        }
        ChunkerME chunker;
        try {
            ChunkerModel chunkerModel = openNLP.getChunkerModel(language);
            if(chunkerModel != null){
                chunker = new ChunkerME(chunkerModel);
            } else {
                log.debug("No Chunker Model for language {}",language);
                chunker = null;
            }
        } catch (IOException e) {
            log.info("Unable to load Chunker Model for language "+language,e);
            chunker = null;
        }
        return chunker;
    }
    protected final PosTypeChunker getPosTypeChunker(String language){
        if(!enableChunker || !enablePosTagger){
            return null;
        }
        PosTypeChunker ptc = posTypeChunkers.get(language);
        if(ptc == null){
            ptc = PosTypeChunker.getInstance(language);
            if(ptc != null){
                posTypeChunkers.put(language, ptc);
            }
        }
        return ptc;
    }

    protected final SentenceDetector getSentenceDetector(String language) {
        if(!enableSentenceDetector){
            return null;
        }
        SentenceDetector sentDetect;
        try {
            SentenceModel sentModel = openNLP.getSentenceModel(language);
            if(sentModel != null){
                sentDetect = new SentenceDetectorME(sentModel);
            } else {
                log.debug("No Sentence Detection Model for language {}",language);
                sentDetect = null;
            }
        } catch (IOException e) {
            log.info("Unable to load Sentence Detection Model for language "+language,e);
            sentDetect = null;
        }
        return sentDetect;
    }

    public final boolean isSimpleTokenizerForced() {
        return forceSimpleTokenizer;
    }

    public final void forceSimpleTokenizer(boolean useSimpleTokenizer) {
        this.forceSimpleTokenizer = useSimpleTokenizer;
    }

    public final boolean isPosTaggerEnable() {
        return enablePosTagger;
    }

    public final void enablePosTagger(boolean enablePosTagger) {
        this.enablePosTagger = enablePosTagger;
    }

    public final boolean isChunkerEnabled() {
        return enableChunker;
    }

    public final void enableChunker(boolean enableChunker) {
        this.enableChunker = enableChunker;
    }

    public final boolean isSentenceDetectorEnabled() {
        return enableSentenceDetector;
    }

    public final void enableSentenceDetector(boolean enableSentenceDetector) {
        this.enableSentenceDetector = enableSentenceDetector;
    }

    public final OpenNLP getOpenNLP() {
        return openNLP;
    }
    public final boolean isPosTypeChunkerEnabled() {
        return enablePosTypeChunker;
    }
    /**
     * Enables the used of the {@link PosTypeChunker} if no {@link Chunker} for
     * the current {@link #getLanguage() language} is available.
     * @param enablePosTypeChunker
     */
    public final void enablePosTypeChunker(boolean enablePosTypeChunker) {
        this.enablePosTypeChunker = enablePosTypeChunker;
        if(!enablePosTypeChunker){
            forcePosTypeChunker(enablePosTypeChunker);
        }
    }

    public final boolean isPosTypeChunkerForced() {
        return forcePosTypeChunker;
    }
    /**
     * Forces the use of the {@link PosTypeChunker} even if a {@link Chunker}
     * for the current language would be available
     * @param forcePosTypeChunker
     */
    public final void forcePosTypeChunker(boolean forcePosTypeChunker) {
        this.forcePosTypeChunker = forcePosTypeChunker;
        if(forcePosTypeChunker) {
            enablePosTypeChunker(true);
        }
    }

    /**
     * Analyses the parsed text in a single chunk. No sentence detector is used
     * @param sentence the sentence (text) to analyse
     * @return the Analysed text
     */
    public AnalysedText analyseSentence(String sentence,String language){
        return new AnalysedText(sentence,language);
    }
    /**
     * Analyses sentence by sentence when {@link Iterator#next()} is called on
     * the returned Iterator. Changes to the configuration of this class will
     * have an effect on the analysis results of this iterator.<p>
     * if no sentence detector is available the whole text is parsed at once. 
     * @param text The text to analyse
     * @param language The language of the parsed text
     * @return Iterator the analyses the parsed text sentence by sentence on
     * calls to {@link Iterator#next()}.
     */
    public Iterator<AnalysedText> analyse(String text,String language){
        return new TextAnalysisIterator(text, language);
    }
    
    private final class TextAnalysisIterator implements Iterator<AnalysedText> {
        private final String text;
        private final Span[] sentenceSpans;
        private int current = 0;
        private final String language;
        private TextAnalysisIterator(String text,String language){
            this.text = text;
            this.language = language;
            if(text == null || text.isEmpty()){
                sentenceSpans = new Span[]{};
            } else {
                SentenceDetector sd = getSentenceDetector(language);
                if(sd != null){
                    sentenceSpans = sd.sentPosDetect(text);
                } else {
                    sentenceSpans = new Span[]{new Span(0, text.length())};
                }
            }
        }
        @Override
        public boolean hasNext() {
            return sentenceSpans.length > current;
        }

        @Override
        public AnalysedText next() {
            Span sentenceSpan = sentenceSpans[current];
            String sentence = sentenceSpan.getCoveredText(text).toString();
            current++; //mark this as consumed and navigate to the next
            return new AnalysedText(sentence,language,sentenceSpan.getStart());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                "Removal of Sentences of the prsed Text is not supported!");
        }
    }

    public class AnalysedText {
        //NOTE: Members are protected to allow the JVM direct access
        /**
         * The analysed sentence
         */
        protected final String sentence;
        /**
         * Final and {@link Collections#unmodifiableList(List) unmodifiable list}
         * with the tokens of the analysed {@link #sentence}.
         */
        protected final List<Token> tokens;
        /**
         * Final and {@link Collections#unmodifiableList(List) unmodifiable list}
         * with the chunks of the analysed {@link #sentence} or <code>null</code>
         * of no chunks are available
         */
        protected final List<Chunk> chunks;
        /**
         * The offset of the sentence with respect to the whole text. Note that
         * {@link AnalysedText this class} only holds the offset and no reference
         * to the whole text. <code>0</code> indicates that this represents the
         * start of the text (this may also indicate that the {@link #sentence} 
         * represents the whole analysed text).
         */
        private final int offset;
        /**
         * The language of the analyzed text
         */
        protected String language;
         
        private AnalysedText(String sentence, String language){
            this(sentence,language,0);
        }
        private AnalysedText(String sentence,String language, int offset){
            if(sentence == null || sentence.isEmpty()){
                throw new IllegalArgumentException(
                    "The parsed Sentence MUST NOT be NULL nor empty!");
            }
            this.sentence = sentence;
            if(language == null || language.isEmpty()){
                throw new IllegalArgumentException("The parsed language MUST NOT be NULL nor empty");
            }
            this.language = language;
            if(offset < 0){
                throw new IllegalArgumentException(
                    "The parsed offset MUST NOT be a negative number (offset="+offset+")");
            }
            this.offset = offset;
            Span[] tokenSpans = getTokenizer(language).tokenizePos(sentence);
            POSTaggerME tagger = getPosTagger(language);
            ChunkerME chunker = getChunker(language);
            PosTypeChunker posTypeChunker = getPosTypeChunker(language);
            String[] tokens = new String[tokenSpans.length];
            for(int ti = 0; ti<tokenSpans.length;ti++) {
                tokens[ti] = tokenSpans[ti].getCoveredText(sentence).toString();
            }
            String[] pos;
            double[] posProbs;
            Span[] chunkSpans;
            double[] chunkProps;
            if(tagger != null){
                pos = tagger.tag(tokens);
                posProbs = tagger.probs();
                if(chunker != null){
                    chunkSpans = chunker.chunkAsSpans(tokens, pos);
                    chunkProps = chunker.probs();
                } else if(posTypeChunker != null){
                    chunkSpans = posTypeChunker.chunkAsSpans(tokens, pos);
                    chunkProps = new double[chunkSpans.length];
                    Arrays.fill(chunkProps, 1.0);
                } else {
                    chunkSpans = null;
                    chunkProps = null;
                }
            } else {
                pos = null;
                posProbs = null;
                chunkSpans = null;
                chunkProps = null;
            }
            List<Token> tokenList = new ArrayList<Token>(tokenSpans.length);
            for(int i=0;i<tokenSpans.length;i++){
                tokenList.add(new Token(tokenSpans[i], tokens[i],
                    pos!=null?pos[i]:null, pos!=null?posProbs[i]:-1));
            }
            //assign the list to the member var but make itunmodifiable!
            this.tokens = Collections.unmodifiableList(tokenList);
            if(chunkSpans != null){
                List<Chunk> chunkList = new ArrayList<Chunk>(chunkSpans.length);
                for(int i=0;i<chunkSpans.length;i++){
                    chunkList.add(new Chunk(chunkSpans[i], chunkProps[i]));
                }
                this.chunks = Collections.unmodifiableList(chunkList);
            } else {
                chunks = null;
            }
            
        }
        public List<Token> getTokens(){
            return tokens;
        }
        public List<Chunk> getChunks(){
            return chunks;
        }
        public String getText(){
            return sentence;
        }
        public String getLanguage(){
            return language;
        }
        
        /**
         * Getter for the Offset of this Sentence relative to the whole analysed
         * Text. <code>0</code> if there is no offset this analysed text represents
         * the whole content
         * @return the offset
         */
        public int getOffset() {
            return offset;
        }

        public class Token {
            //NOTE: Members are protected to allow the JVM direct access
            protected final Span span;
            protected String token;
            protected final String pos;
            protected final double posProbability;

            private Token(Span span,String token,String pos,double posProbability){
                this.span = span;
                this.pos = pos;
                this.token = token;
                this.posProbability = posProbability;
            }

            public int getStart(){
                return span.getStart();
            }
            public int getEnd(){
                return span.getEnd();
            }
            public String getPosTag(){
                return pos;
            }
            /**
             * @return the POS probability
             */
            public double getPosProbability() {
                return posProbability;
            }
            /**
             * Getter for the value of this token
             * @return
             */
            public String getText(){
                if(token == null){
                    token = span.getCoveredText(sentence).toString();
                }
                return token;
            }
            @Override
            public String toString() {
                return getText()+(pos != null?'_'+pos:"");
            }
        }
        public class Chunk {
            //NOTE: Members are protected to allow the JVM direct access
            /**
             * The span over the char offset of this chunk within the 
             * {@link AnalysedText#sentence}
             */
            protected final Span span;
            /**
             * Span over the {@link AnalysedText#tokens} as used by the
             * {@link #getStart()} and {@link #getEnd()} methods 
             */
            protected final Span chunkSpan;
            protected final double probability;
            /**
             * DO NOT DIRECTLY ACCESS - lazy initialisation in {@link #getText()}
             */
            private String __text;
            /**
             * DO NOT DIRECTYL ACCESS - lazy initialisation in {@link #getTokens()}
             */
            private List<Token> __chunkTokens;

            private Chunk(Span chunkSpan,double probability){
                this.chunkSpan = chunkSpan;
                this.span =  new Span(tokens.get(chunkSpan.getStart()).getStart(), 
                    tokens.get(chunkSpan.getEnd()).getEnd());
                this.probability = probability;
            }
            public List<Token> getTokens(){
                if(__chunkTokens == null){
                    __chunkTokens = tokens.subList(chunkSpan.getStart(), chunkSpan.getEnd());
                }
                return __chunkTokens;
            }
            /**
             * @return the span
             */
            public int getStart() {
                return chunkSpan.getStart();
            }
            public int getEnd(){
                return chunkSpan.getEnd();
            }
            public int getSize(){
                return chunkSpan.length();
            }
            /**
             * @return the probability
             */
            public double getProbability() {
                return probability;
            }
            /**
             * The text of this chunk
             * @return
             */
            public String getText(){
                if(__text == null){
                    __text = span.getCoveredText(sentence).toString();
                }
                return __text;
            }
            @Override
            public String toString() {
                return getText();
            }
        }
    }    
}