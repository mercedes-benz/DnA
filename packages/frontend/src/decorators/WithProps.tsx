import * as React from 'react';

export function withProps(Comp: React.ComponentClass<any>, newProps: any): React.ComponentClass {
  return class extends React.Component {
    public render() {
      /* @ts-ignore */
      return <Comp {...this.props} {...newProps} {...this.state} />;
    }
  };
}
