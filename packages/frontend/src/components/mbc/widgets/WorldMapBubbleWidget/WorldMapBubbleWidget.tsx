import Popper from 'popper.js';
import * as React from 'react';
import { ComposableMap, Geographies, Geography, Marker } from 'react-simple-maps';
import * as geodata from 'globals/maps/countries-110m.json';
import { ILocationsMapChartDataItem } from 'globals/types.js';

export interface IWorldMapBubbleWidgetProps {
  data: ILocationsMapChartDataItem[];
  onLocationMarkerClick: (value: any) => void;
}

export default class WorldMapBubbleWidget extends React.Component<IWorldMapBubbleWidgetProps, {}> {
  // protected geoUrl = 'https://cdn.jsdelivr.net/npm/world-atlas@2/land-10m.json';

  protected popper: Popper = undefined;
  constructor(props: IWorldMapBubbleWidgetProps) {
    super(props);
  }

  public render() {
    const data = this.props.data;
    return (
      <div className="chart-wrapper world-map">
        <ComposableMap projection="geoEquirectangular" height={300} projectionConfig={{ scale: 120 }}>
          <Geographies geography={geodata}>
            {({ geographies }) =>
              geographies.map((geo) => (
                <Geography key={geo.rsmKey} geography={geo} fill="#383F49" stroke="#252a33" strokeWidth="1" />
              ))
            }
          </Geographies>
          {data.map((marker: ILocationsMapChartDataItem) => {
            const { countryName, coordinates } = marker;
            return (
              <Marker
                key={countryName}
                coordinates={coordinates}
                onMouseEnter={this.onMarkerMouseEnter(marker)}
                onMouseLeave={this.onMarkerMouseLeave}
              >
                <circle
                  r={8}
                  fill="#00adef"
                  cursor="pointer"
                  stroke="#252a33"
                  // tslint:disable-next-line: jsx-no-lambda
                  onClick={() => this.onMarkerClick(marker)}
                />
              </Marker>
            );
          })}
        </ComposableMap>
      </div>
    );
  }

  protected onMarkerMouseEnter = (marker: ILocationsMapChartDataItem) => {
    return (event: React.MouseEvent<SVGPathElement>) => {
      const { countryName, countValue } = marker;
      const tooltipElement = document.createElement('DIV');
      tooltipElement.classList.add('tooltip', 'map-chart');
      tooltipElement.innerHTML = `<span>${countValue} Solutions</span> in ${countryName}`;
      document.body.append(tooltipElement);
      setTimeout(() => {
        tooltipElement.classList.add('show');
      }, 20);
      this.popper = new Popper(event.currentTarget, tooltipElement, { placement: 'top' });
    };
  };

  protected onMarkerMouseLeave = () => {
    const elem = document.querySelector('.tooltip');
    if (elem) {
      elem.parentNode.removeChild(elem);
    }
    this.popper.destroy();
  };

  protected onMarkerClick = (marker: ILocationsMapChartDataItem) => {
    this.onMarkerMouseLeave();
    this.props.onLocationMarkerClick(marker);
  };
}
