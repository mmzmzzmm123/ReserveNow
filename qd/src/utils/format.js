/**
 * Format date
 * @param {string|Date} date Date to format
 * @param {string} fmt Format pattern, default yyyy-MM-dd HH:mm:ss
 * @returns {string} Formatted date string
 */
export function formatDate(date, fmt = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return '';
  
  if (typeof date === 'string') {
    date = new Date(date);
  }
  
  const o = {
    'M+': date.getMonth() + 1, // Month
    'd+': date.getDate(), // Day
    'H+': date.getHours(), // Hour
    'm+': date.getMinutes(), // Minute
    's+': date.getSeconds(), // Second
    'q+': Math.floor((date.getMonth() + 3) / 3), // Quarter
    'S': date.getMilliseconds() // Millisecond
  };

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }

  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      );
    }
  }
  
  return fmt;
} 