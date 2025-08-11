class Exception extends Error {
  // @ts-ignore
  static name: string;

  constructor(message: string) {
    super(message);
    this.name = new.target.name; // safer in TS
    if ('captureStackTrace' in Error) {
      (Error as any).captureStackTrace(this, this.constructor);
    }
  }
}

class BusinessException extends Exception {
}

class NotFoundException extends Exception {
}

class ValidationException extends Exception {
}

type exception =
  NotFoundException
  | ValidationException
  | BusinessException
  | Exception
  | Error;

function catchError<T extends Error>(
  e: unknown,
  map: { [key: string]: (err: T) => void }
) {
  if (e instanceof Error) {
    const name = (e.constructor as unknown as { name: string }).name;
    const handler = map[name];
    if (handler) {
      handler(e as T);
      return;
    }
  }
  console.error("Unhandled error", e);
}

function handleExceptions<T>(
  fn: () => T,
  map: { [key: string]: (err: Error) => void }
): T | undefined {
  try {
    return fn();
  } catch (e) {
    catchError(e, {
      ValidationError: (err) => console.log("Validation error:", err.message),
      NotFoundError: (err) => console.log("Not found:", err.message),
    })
  }
}

// Define a configuration object with specific types for its properties
const config = {
  port: 3000,
  host: 'localhost'
} satisfies Record<string, string | number>;

// const assertion to ensure the config object matches the expected type
const routes = {
  home: '/',
  about: '/about',
  dashboard: '/dashboard',
  settings: '/settings'
} as const;

// 'onClick', 'onChange', 'onSubmit', etc. are common event names in JavaScript
type EventName = 'on${Capitalize<string>}'

// 'GET /api/users', 'POST /api/users', etc. are common API routes
type Method = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
type Endpoint = '/api/${string}';
type Route = '${Method} ${Endpoint}';
function makeReq(route: string) {
}
makeReq('GET /api/users'); // Example usage

// Discriminated union type for State
type State =
  | { type: 'idle' }
  | { type: 'loading' }
  | { type: 'success', data: string }
  | { type: 'error', error: Error };

function handleState(state: State) {
  switch (state.type) {
    case 'idle':
      console.log('State is idle');
      break;
    case 'loading':
      console.log('State is loading');
      break;
    case 'success':
      console.log('State is success with data:', state.data.toUpperCase());
      break;
    case 'error':
      console.error('State is error with message:', state.error.message);
      break;
    default:
      throw new Error('Unknown state type');
  }
}

// Type Predicate for custom type guard
function isString(value: unknown): value is string {
  return typeof value === 'string';
}

// Index access type for arrays
type ArrayElement<T extends readonly unknown[]> = T extends readonly (infer U)[] ? U : never;
type T = { [key: string]: unknown };
type userKey = keyof T; // 'userKey' is a type that can be any key of T

function IAccess(p: keyof T, o: T): T {
  return { [p]: o }
}

// Conditional type for dynamic type logic
type IsArray<T> = T extends any[] ? true : false;
type IsString<T> = T extends string ? true : false;
type IsNumber<T> = T extends number ? true : false;

// Extract array element type
type Flatten<T> = T extends Array<infer U> ? U : T;
type FlattenedString = Flatten<string[]>; // FlattenedArray is string
type FlattenedNumber = Flatten<number>; // FlattenedNumber is number

type ApiResponse<T> = T extends { error: string }
  ? { success: false; error: string }
  : { success: true; data: T };

// Utility type is your friends
type PartialUser = Partial<T>
// type Readonly = Readonly<T>
type RequiredEmail = Omit<any, 'email'> & { email: string };
type JustEmailAndId = Pick<T, 'email' | 'id'>;

// Function Overloads signature
function parse(value: string): object;
function parse(value: string, reviver: Function): object;

// Impl signature
function parse(value: string, reviver?: Function): object {
  return JSON.parse(value);
}

// Usage gets proper type hints
const o1 = parse('{}')
const o2 = parse('{}', (key, value) => value)
function createElement(tag: 'img'): HTMLImageElement
function createElement(tag: 'input'): HTMLInputElement;
function createElement(tag: string): HTMLElement;
function createElement(tag: string): HTMLElement {
  return document.createElement(tag);
}

const img = createElement('img')
const input = createElement('input');



