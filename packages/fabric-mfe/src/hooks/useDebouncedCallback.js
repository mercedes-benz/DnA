import { useRef, useEffect, useCallback } from 'react';  
  
export const useDebouncedCallback = (callback, delay = 500) => {  
  const callbackRef = useRef(callback);  
  const timerRef = useRef(null);  
  
  useEffect(() => {  
    callbackRef.current = callback;  
  }, [callback]);  
  
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
  
  debounced.cancel = useCallback(() => {  
    if (timerRef.current) {  
      clearTimeout(timerRef.current);  
      timerRef.current = null;  
    }  
  }, []);  
  
  return debounced;  
};