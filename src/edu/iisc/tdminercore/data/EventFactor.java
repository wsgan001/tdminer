/*
 * EventFactor.java
 *
 * Created on November 2, 2006, 2:28 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.util.PickMatrix;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Comparator;

/**
 * Event Factor.
 * It is convenient for event types to have arbitrarily long names.
 * It is inconvenient to compute with long names for events.
 * Therefore, the event type names may be looked up via an array index.
 * Also, the array index may be looked up using the event type name via a hash map.
 * For practical purposes events only need to be added to the set.
 *
 * @author phreed@gmail.com
 */
public class EventFactor 
        implements Iterable<EventFactor.EventType>, Serializable
{
    protected int nextOrdinal = 0;
    static public float edgeScale = 50.0F;
    int count = 0;
        
    public class EventType implements Serializable
    {
        public int id;          // a numeric key by which this event type is known
        public String name;     // a text key by which this event type is known
         
        public String alias;    // an alternate name
        private int votes;       // how many times does this event type occur in the input
        public int ordinal;     // a field reflecting the sorted order
        public float reverse;     // a field reflecting the reversed order
        
        private float[] position = new float[2];  // used to specify spatial position
        private boolean posSet = false;
        
        /*
         * The id for the new event is assumed to be the next ordinal
         */
        private EventType(String name) 
        {
            this(EventFactor.this.nextOrdinal,name);            
            EventFactor.this.nextOrdinal++;
        }
        
        private EventType(Integer id, String name) 
        {
            String trimName = name.trim();
            this.name = trimName;
            this.alias = trimName;
            this.votes = 0;
            this.id = id;
            this.ordinal = this.id;
            this.reverse = -this.ordinal * edgeScale;
            
            // build a position from the first two characters of the name
            this.position[0] = 0.0F;
            this.position[1] = 1.0F;
            if (trimName.length() < 1) return;
            this.position[0] = (1.0F + trimName.charAt(0) - '0') * edgeScale;
            
            if (trimName.length() < 2) return;
            this.position[1] = (1.0F + trimName.charAt(1) - '0') * edgeScale;     
        }
        
        public void setPosition( float xpos, float ypos ) {
            this.position[0] = xpos;
            this.position[1] = ypos;
            this.posSet = true;
        }
        
        public boolean isPosSet() 
        {
            return this.posSet;
        }
        
        public float getX()
        {
               return this.position[0];
        }
        
        public float getY()
        {
               return this.position[1];
        }
        
        @Override
        public String toString()
        {
            return name;
        }
    }
    /**
     * types - a list of the event type's names, as the ids are assigned 
     *   sequentially, starting from 0, the index in the 'names' list is
     *   identical to the event type's id.
     * name2type - a hash map of event type names to event types.
     * id2type - a hash map of event type ids to event types.
     */
    private List<EventType> types;
    private Map<String, EventType> name2type;
    private Map<Integer, EventType> id2type;
//    private int samplesize = 0;
    
    public void resetCounts()
    {
        for(EventType e : types) e.votes = 0;
    }
    
    private void allocCollections(int size)
    {
        types = new ArrayList<EventType>(size);  
        name2type = new HashMap<String, EventType>(size);
        id2type = new HashMap<Integer, EventType>(size);
    }
    /**
     * Creates a new instance of EventFactor
     */
    public EventFactor() {
        allocCollections(20);
    }
    
    /**
     * Creates a new instance of EventFactor and addes the event types.
     * In this case the event type ids are autogenerated in sequence.
     * 
     * @param theNames 
     *      a list of event type names
     */
    public EventFactor(String[] theNames) {
        allocCollections(theNames.length);
        for (int lastIndex=0; lastIndex < theNames.length; lastIndex++) 
        {
            this.add(lastIndex,theNames[lastIndex]);
        }
    } 
    /**
     * This constructor takes a string containing a list of episode names.
     */
    public EventFactor(String theNames) {
        String[] names = theNames.split("\\s+");
        allocCollections(names.length);
        int nextIndex = 0;
        for (int lastIndex=0; lastIndex < names.length; lastIndex++) 
        {
            if (names[lastIndex].length() < 1) continue;
            this.add(nextIndex,names[lastIndex]);
            nextIndex++;
        }
    } 
     
    public class PositionError extends Exception
    {
        public PositionError(String message) {
            super(message);
        }
    }
    public void setPosition( String request ) 
        throws PositionError
    {
        String[] frag = request.split("\\s+");
        
        try {
            float xpos = Float.parseFloat( frag[1].trim() );
            float ypos = Float.parseFloat( frag[2].trim() );
            String trimName = frag[0].trim();
            if (! name2type.containsKey( trimName )) {
                System.err.println("event type not found: " + trimName);
                throw new PositionError("could not parse request");
            }
            EventType type = name2type.get( trimName );
            type.setPosition( xpos, ypos ); 
        } catch (NumberFormatException ex) {
            throw new PositionError("could not parse request");
        } 
    }
    
    public Iterator<EventType> iterator() {
        return types.iterator();
    }
    
    /** 
     * @return the largest event id.
     */
    public Integer getMaximumId() 
    {
        Integer id = 0;
        for( EventType type : this.types ) {
            if (type.id <= id) continue;
            id = type.id;
        }
        return id;
    }
    /** 
     * @return the smalllest event id.
     */
    public Integer getMinimumId() 
    {
        Integer id = Integer.MAX_VALUE;
        for( EventType type : this.types ) {
            if (id <= type.id) continue;
            id = type.id;
        }
        return id;
    }
    
    public void incrById(int id)
    {
        EventType current = id2type.get(id);
        current.votes ++;
        count ++;
    }
    /** 
     * Add a new event type to the set.
     * If the name is already present then increment its votes.
     * If not present then add it to the list.
     *
     * @param name the name of the new event type.
     * @return the id of the event type named.
     */
    public int put(String name) 
    {
        String trimName = name.trim();
        if (name2type.containsKey( trimName )) {
            EventType current = name2type.get(trimName);
            //current.votes++;
            return current.id;
        }
        int id = types.size();
        this.add(id,trimName);
        return id;
    }
    /**
     * Similar to {@link put(String)} but forces the id to the specified value.
     * @param id - the numeric id used in the event stream
     * @param name - the well known name of the event type
     */
    public boolean put(Integer id, String name) 
    { 
        String trimName = name.trim();
        if (id2type.containsKey(id)) {
            return false; // problem, throw an exception?
        }
        if (name2type.containsKey(trimName)) {
            EventType current = name2type.get(trimName);
            //current.votes++;
            return true;
        }
        this.add(id,trimName);
        return true;
    }
    /*
     * A private function introduced to keep the three collections consistent.
     * The collection should always be updated through this method.
     */
    private void add(Integer id, String name) {
        String trimName = name.trim();
        EventType newtype = new EventType(id,trimName);
        types.add(newtype);
        id2type.put(id,newtype);
        name2type.put(trimName,newtype);
    }
    
    /**
     * Make a clone of an existing factor.
     * The clone differs from its parent in that the ids have no gaps and
     * will be in list order.
     *
     * @param that - the factor to clone
     */
    public void addAll(EventFactor that) {
        for (ListIterator<EventType> it = that.types.listIterator(); it.hasNext(); )
        {
            put(it.next().name);
        }
     }
     
    /** 
     * No provision is to be made to remove types.
     * This class is to provide bind once variables.
     */
    public boolean remove(String name) {
        return false;
    }
    /**
     * However, the alias may be changed at will.
     */
    public void setAlias(int id, String alias) {
        id2type.get(id).alias = alias;
    }
    public void setAlias(String name, String alias) {
        String trimName = name.trim();
        name2type.get(trimName).alias = alias;
    }
    
        
    /** Retrieves the id of the named event
     * @param name the name of the event sought.
     * @return the id of the event type named.
     */
    public int getId(String name) {
        String trimName = name.trim();
        if (!name2type.containsKey(name)) return -1;
        return name2type.get(trimName).id;
    }
    
    /** Retrieves the name of the event having a particular id.
     * @param id the event type id.
     * @return the name of the event type.
     */
    public String getName(int id) {
        if (!id2type.containsKey(id)) return null;
        return id2type.get(id).name;
    }
    
    public EventType get(int id) {
        if (!id2type.containsKey(id)) return null;
        return id2type.get(id);
    }
    
    public EventType get(String name) {
        String trimName = name.trim();
        if (!name2type.containsKey(trimName)) return null;
        return name2type.get(trimName);
    }
    
    public int getSize() { return types.size(); }
    
    /** 
     * This method returns an instantaneous serial episode list, 
     * one episode for each event type.
     * The length of each episode is one.
     * It could be argued that an episode of unary length isn't an episode
     * at all, but by implementing the IEpisode interface it can be 
     * treated along with other 'true' episodes. 
     */
    public List<IEpisode> getEpisodeList()
    {
        List<IEpisode> episodeList = new ArrayList<IEpisode>();
        for (int ix=0; ix < this.types.size(); ix++ ) 
        {
            IEpisode episode = new Episode(1,this);
            episode.setEvent(0,ix);
            episode.setVotes(0, this.types.get(ix).votes);
            episode.setSampleSize(0, count);
            episodeList.add(episode);
        }
        return episodeList;
    }
    
    /**
     * Produce a generalized serial episode list, N episodes for each event type.
     * Where N may be the number of...
     * single - ...duration intervals specified.
     * product - ...duration interval combinations.
     * 
     * @param durations 
     *      the time intevals defining the outer bounds for event duration.
     */
    public List<IEpisode> 
            getGeneralizedEpisodeList(List<Interval> durations, int dimension)
    {
        List<IEpisode> episodeList = new ArrayList<IEpisode>();
        
        for (int ix=0; ix < this.types.size(); ix++ ) 
        {
            int[] events = {ix};
            Iterator<Integer> mapit;
            switch (dimension) {
                case 1: 
                    mapit = new PickMatrix.PermutorSingle(durations.size());
                    break;
                default:
                    mapit = new PickMatrix.PermutorFull(durations.size());
                    break;
            }
            
            while (mapit.hasNext())
            {
                int[] map = { mapit.next().intValue() };
                IEpisode episode = 
                        new GeneralizedEpisode(events,this, map, durations);
                
                episode.setVotes(0, 0); // the votes cannot be effectively used here.
                episodeList.add(episode);
            }
        }
        return episodeList;
    }

    public Vector<String> asVector() {
        Vector<String> vector = new Vector<String>();
        for (ListIterator<EventType> it = this.types.listIterator(); it.hasNext(); )
        {
            vector.add(it.next().name);
        }
        return vector;
    }
    
    /* 
     * Set the ordinal value based on the order mode presented.
     * byNameLexical    - in ascending alphanumeric order
     * byNameNumeric    - try to treat the values as numbers, otherwise alphabetic
     * byOnsetTime      - in the order they were defined (input data order)
     * byVoteCount      - by most frequent event type
     */
    public enum OrderMode 
    {
        byNameLexical ("nameLexical"),
        byNameNumeric ("nameNumeric"), 
        byOnsetTime   ("onsetTime"),
        byVoteCount   ("voteCount");
        
        private String name;
        OrderMode (String aname) { name = aname; }
        public String getName() { return name; }
    };
    private OrderMode order = OrderMode.byNameLexical;
    
    public String getOrdinal() { return this.order.getName(); }
    public void setOrdinal(String order)
    {
        order = order.trim();
        for( OrderMode mode : OrderMode.values()) {
            if (!mode.getName().equalsIgnoreCase(order)) continue;
            setOrdinal(mode);
            return;
        }
        setOrdinal(OrderMode.byNameLexical);
    }
    public void setOrdinal(OrderMode order)
    {
        // System.out.println("setOrdinal = " + order);
       
        this.order = order; // used by the getLabels iterator and getOrdinal
        switch (order) {
            case byNameLexical: 
            { 
                List<EventType> typesSorted = new ArrayList<EventType>();
                for( EventType type : this.types ) { typesSorted.add(type); }
                Collections.sort(typesSorted, 
                        new Comparator<EventType>() {
                        public int compare(EventType lhs, EventType rhs) {
                            return lhs.name.compareTo(rhs.name);
                        }});
                int ix = 0;
                for( EventType type : typesSorted ) {
                    type.ordinal = ix;
                    ix++;
                }
                break;
            }
            
            /* not strictly numeric as alphabetic values are 
             replaced with negative values */
            case byNameNumeric:
            {
                List<EventType> typesSorted = new ArrayList<EventType>();
                for( EventType type : this.types ) { typesSorted.add(type); }
                try {
                    Collections.sort(typesSorted, 
                            new Comparator<EventType>() {
                            public int compare(EventType lhs, EventType rhs) {
                                int lhsNum = Integer.parseInt(lhs.name , 10);
                                int rhsNum = Integer.parseInt(rhs.name , 10);

                                if (lhsNum < rhsNum) return -1;
                                else if (lhsNum > rhsNum) return 1;
                                return 0;
                            }});
                    int ix = 0;
                    for( EventType type : typesSorted ) {
                        type.ordinal = ix;
                        ix++;
                    }
                }
                catch (NumberFormatException ex) {
                    System.out.println("Could not parse name into number");
                }
                break;  
            }
       
            case byVoteCount:
            {
                List<EventType> typesSorted = new ArrayList<EventType>();
                for( EventType type : this.types ) { typesSorted.add(type); }
                Collections.sort(typesSorted, 
                        new Comparator<EventType>() {
                        public int compare(EventType lhs, EventType rhs) {
                            if (lhs.votes < rhs.votes) return -1;
                            else if (lhs.votes > rhs.votes) return 1;
                            return 0;
                        }});
                int ix = 0;
                for( EventType type : typesSorted ) {
                    type.ordinal = ix;
                    ix++;
                }
                break;
            } 
            
            case byOnsetTime:
            {
                for(EventType type : types) { type.ordinal = type.id; }
                break;
            }
            
        }
        for( EventType type : this.types ) 
        {
            type.reverse = this.types.size() - type.ordinal;
        }
    }
    
    /* 
     * Produce a list of labels in ordinal order.
     */
    public class OrderedLabel implements Serializable
    {
        public String text;
        public int ordinate;
        public OrderedLabel(int ordinate, String text) {
            this.ordinate = ordinate;
            this.text = text;
        }
    }
    
    public enum DisplayMode { ofName, ofAlias, ofOrdinal };
    private DisplayMode display;
    
    public List<OrderedLabel> getLabels(DisplayMode mode) 
    {
        List<EventType> typesSorted = new ArrayList<EventType>();
        for( EventType type : this.types ) {
            typesSorted.add(type);
        }
        Collections.sort(typesSorted, 
                new Comparator<EventType>() {
                public int compare(EventType lhs, EventType rhs) {
                    return lhs.ordinal < rhs.ordinal ? -1 
                            : lhs.ordinal > rhs.ordinal ? 1
                            : 0;
                }});

        switch (mode) {
            case ofName:
            {
                List<OrderedLabel> names = new ArrayList<OrderedLabel>();
                for( EventType type : typesSorted ) {
                    names.add(new OrderedLabel(type.ordinal,type.name));
                }
                return names;
            }
            case ofAlias:
            {
                List<OrderedLabel> alias = new ArrayList<OrderedLabel>();
                for( EventType type : typesSorted ) {
                    alias.add(new OrderedLabel(type.ordinal,type.alias));
                }
                return alias;
            }
            case ofOrdinal:
            { 
                List<OrderedLabel> ordinalStr = new ArrayList<OrderedLabel>();
                for( EventType type : typesSorted ) {
                    ordinalStr.add(new OrderedLabel(type.ordinal, String.valueOf(type.ordinal)));
                }
                return ordinalStr;
            }
            default:
                return null;
        }
    }
    
     /**
      * serialize the object as a string
      */
     public String toString() {
         StringBuffer buf = new StringBuffer();
         int ix = 0;
         for( EventType type : this.types) {    
            buf.append("[" + ix + "]");
            buf.append(type.name); 
            if (type.alias != null) buf.append("(" + type.alias + ")");
            buf.append(":" + type.votes);
            buf.append('\n');
            ix++;
	}
	return buf.toString();
     }
     
     public static int compareEvent(int id1, EventFactor f1, int id2, EventFactor f2) 
     {
         EventFactor.EventType t1 = f1.types.get(id1);
         EventFactor.EventType t2 = f2.types.get(id2);
         return t1.name.compareTo(t2.name);
     }
 
}
