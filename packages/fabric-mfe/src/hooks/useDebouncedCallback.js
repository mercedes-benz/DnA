// packages/fabric-mfe/src/hooks/useDebouncedCallback.js  
import { useRef, useEffect, useCallback } from 'react';  
  
export const useDebouncedCallback = (callback, delay = 500) => {  
  // always hold the latest callback so there is no stale-closure problem  
  const callbackRef = useRef(callback);  
  const timerRef = useRef(null);  
  
  useEffect(() => {  
    callbackRef.current = callback;  
  }, [callback]);  
  
  // clear any pending timer on unmount  
  useEffect(() => {  
    return () => {  
      if (timerRef.current) {  
        clearTimeout(timerRef.current);  
      }  
    };  
  }, []);  
  
  const debounced = useCallback((...args) => {  
    if (timerRef.current) {  
      clearTimeout(timerRef.current);  
    }  
    timerRef.current = setTimeout(() => {  
      callbackRef.current(...args);  
    }, delay);  
  }, [delay]);  
  
  // optional manual cancel  
  debounced.cancel = useCallback(() => {  
    if (timerRef.current) {  
      clearTimeout(timerRef.current);  
      timerRef.current = null;  
    }  
  }, []);  
  
  return debounced;  
};