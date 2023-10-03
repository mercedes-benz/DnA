import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './PersonaSelect.scss';

export interface IPersonaSelectProps {
  personas: any;
  selectedPersonasList: string[];
  isSummary: boolean;
  onChangePersonas: (e:any) => void;
}

const PersonaSelect = (props: IPersonaSelectProps) => {
  const {selectedPersonasList} = props;
  const [selectedPersona, setSelectedPersona] = useState(selectedPersonasList !== null ? selectedPersonasList : []);

  const handleOnChange = (e:any) => {
    const { value, checked } = e.target;
    if (checked) {
      setSelectedPersona([...selectedPersona, value]);
    } else {
      setSelectedPersona(selectedPersona.filter((persona:any) => persona !== value));
    }
  }

  useEffect(() => {
    props.onChangePersonas(selectedPersona);
  }, [selectedPersona]); 

  return (
    <div className={Styles.container}>
      <h2 className={classNames(Styles.heading, props.isSummary? Styles.summaryHeading : '')}>Personas</h2>
      <div className={Styles.personaContainer}>
        {
          props.personas.map((persona:any) => {
            return (
              <div key={persona.id} className={classNames(Styles.persona, selectedPersona.length > 0 ? props.isSummary ? selectedPersona?.includes(persona.value) : selectedPersona?.includes(persona.value) && Styles.selected : '')}>
                <div className={Styles.personaAvatar}>
                  <img src={persona.avatar} />
                  <p>{persona.name}</p>
                </div>
                <div className={Styles.desc}>
                  <p>{persona.description}</p>
                </div>
                {!props.isSummary? 
                  <div className={Styles.personaSelect}>
                    <label className={classNames('checkbox', Styles.checkboxItem)}>
                      <span className={classNames('wrapper', Styles.thCheckbox)}>
                        <input
                          type="checkbox"
                          className="ff-only"
                          id={persona.id}
                          value={persona.value}
                          checked={selectedPersona.length > 0 ? selectedPersona?.includes(persona.value) : false}
                          onChange={(e) => handleOnChange(e)}
                        />
                      </span>
                    </label>
                  </div>
                :''}
                
              </div>
            )
          })
        }
        {selectedPersona.length===0 && <div className={Styles.noPersonaSelected}>NA</div>}
      </div>
    </div>
  );
};

export default PersonaSelect;