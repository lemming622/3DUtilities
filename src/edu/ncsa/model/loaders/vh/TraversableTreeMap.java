package edu.ncsa.model.loaders.vh;
import java.util.*;

/**
 * A simple extension of the TreeMap class to allow for a method that
 * retrives the key coming imedietley after a particular key.
 *  @param <K> the key type
 *  @param <V> the value type
 *  @author Kenton McHenry
 */
public class TraversableTreeMap<K,V> extends TreeMap<K,V>
{
  private boolean MODIFIED = false;
  private TreeMap<K,K> next_key = null;
  private TreeMap<K,K> prev_key = null;
  
  /**
   * Add a key/value pair to the tree map.
   *  @param key key with which the specified value is to be associated
   *  @param value value to be associated with the specified key
   *  @return the previous value associated with the specified key
   */
  public V put(K key, V value)
  {
    MODIFIED = true;
    return super.put(key, value);
  }
  
  /**
   * Returns the key to which the specified key is mapped, or null if this map contains no mapping for the key.
   *  @param key_value the key value whose nearest key is to be returned
   *  @return the nearest key
   */
  public K getKey(K key_value)
  {
    K key = lowerKey(key_value);
    
    if(key == null){
      key = higherKey(key_value);
    }
    
    return key;
  }
  
  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
   *  @param key_value the key whose associated value is to be returned
   *  @return the value to which the specified key is mapped, or null if this map contains no mapping for the key 
   */
  public V getValue(K key_value)
  {
    return get(getKey(key_value));
  }
  
  /**
   * Rebuild the previous/next maps if anything has been added to the tree.
   */
  private void rebuild()
  {
    if(MODIFIED){
      K[] keys = (K[])super.keySet().toArray();
      next_key = new TreeMap<K,K>();
      prev_key = new TreeMap<K,K>();
      prev_key.put(keys[0], keys[keys.length-1]);
      
      for(int i=0; i<keys.length-1; i++){
        next_key.put(keys[i], keys[i+1]);
        prev_key.put(keys[i+1], keys[i]);
      }
      
      next_key.put(keys[keys.length-1], keys[0]);
      
      MODIFIED = false;
    }
  }
  
  /**
   * Get the key next in line from the given key.
   *  @param key a key in the map
   *  @return the key following the given key
   */
  public K nextKey(K key)
  {
    rebuild();
    
    if(next_key != null){
      return next_key.get(key);
    }else{
      return null;
    }
  }
  
  /**
   * Get the key prior to the given key.
   *  @param key the key in the map
   *  @return the key prior to the given key
   */
  public K prevKey(K key){
    if(prev_key != null){
      return prev_key.get(key);
    }else{
      return null;
    }
  }
  
  /**
   * Get a list of the keys used.
   *  @return the vector of keys
   */
  public Vector<K> getKeys()
  {
    Vector<K> keys = new Vector<K>();
    Iterator itr = super.keySet().iterator();
    
    while(itr.hasNext()){
      keys.add((K)itr.next());
    }
    
    return keys;
  }
}